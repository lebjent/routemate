package com.trip.routemate.common.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * API 요청과 응답을 추적 ID 기준의 JSON 로그로 남긴다.
 *
 * JSON 본문만 기록하며 비밀번호·토큰처럼 민감한 값은 마스킹한다.
 */
@Component
@RequiredArgsConstructor
@NullMarked
public class HttpExchangeLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HttpExchangeLoggingFilter.class);
    private static final Logger exchangeLog = LoggerFactory.getLogger("http.exchange");
    private static final int MAX_BODY_LENGTH = 16 * 1024;
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "pwd", "userpwd", "ownerpassword", "authorization", "token", "accesstoken", "refreshtoken"
    );

    private final ObjectMapper objectMapper;

    /** API 요청과 무관한 경로 및 CORS 사전 요청은 기록 대상에서 제외한다. */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/") || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /** 요청과 응답 본문을 캐싱한 뒤 처리 시간과 함께 구조화 로그로 기록한다. */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    )
            throws ServletException, IOException {
        var cachedRequest = new ContentCachingRequestWrapper(request, MAX_BODY_LENGTH);
        var cachedResponse = new ContentCachingResponseWrapper(response);
        var startedAt = System.nanoTime();
        var traceId = traceId(request);
        MDC.put("traceId", traceId);
        cachedResponse.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(cachedRequest, cachedResponse);
        } finally {
            try {
                writeExchangeLog(cachedRequest, cachedResponse, (System.nanoTime() - startedAt) / 1_000_000);
                cachedResponse.copyBodyToResponse();
            } finally {
                MDC.remove("traceId");
            }
        }
    }

    /** 캐싱된 HTTP 교환 정보를 보기 쉬운 JSON으로 변환해 기록한다. */
    private void writeExchangeLog(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long durationMs) {
        try {
            ObjectNode exchange = objectMapper.createObjectNode();
            exchange.put("event", "http_exchange");
            exchange.put("traceId", MDC.get("traceId"));
            exchange.put("method", request.getMethod());
            exchange.put("path", request.getRequestURI());
            exchange.set("query", queryNode(request));
            exchange.put("status", response.getStatus());
            exchange.put("durationMs", durationMs);
            exchange.put("requestContentType", nullToEmpty(request.getContentType()));
            exchange.put("responseContentType", nullToEmpty(response.getContentType()));
            exchange.set("requestBody", bodyNode(request.getContentAsByteArray(), request.getContentType(), request.getCharacterEncoding()));
            exchange.set("responseBody", bodyNode(response.getContentAsByteArray(), response.getContentType(), response.getCharacterEncoding()));
            exchangeLog.info("{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exchange));
        } catch (JsonProcessingException exception) {
            log.warn("HTTP_EXCHANGE logging failed: {} {}", request.getMethod(), request.getRequestURI(), exception);
        }
    }

    /** 본문이 JSON일 때만 파싱하고, 그 외 형식이나 잘못된 JSON은 요약 정보만 남긴다. */
    private JsonNode bodyNode(byte[] body, @Nullable String contentType, @Nullable String encoding) {
        if (body.length == 0) return objectMapper.nullNode();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE)) {
            return objectMapper.createObjectNode().put("omitted", "non-json body").put("length", body.length);
        }
        try {
            Charset charset = resolveCharset(encoding);
            JsonNode parsed = objectMapper.readTree(new String(body, charset));
            maskSensitiveValues(parsed);
            return parsed;
        } catch (Exception ignored) {
            return objectMapper.createObjectNode().put("omitted", "invalid json body").put("length", body.length);
        }
    }

    /** 요청 인코딩이 없거나 Servlet 기본값일 경우 UTF-8을 사용한다. */
    private @org.jspecify.annotations.NonNull Charset resolveCharset(@Nullable String encoding) {
        if (encoding == null || encoding.isBlank() || "ISO-8859-1".equalsIgnoreCase(encoding)) {
            return Objects.requireNonNull(StandardCharsets.UTF_8);
        }
        return Objects.requireNonNull(Charset.forName(encoding));
    }

    /** JSON 트리를 순회하며 키 이름으로 식별한 민감 값을 마스킹한다. */
    private void maskSensitiveValues(@Nullable JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node instanceof ObjectNode objectNode) {
            Iterator<String> names = objectNode.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                JsonNode value = objectNode.get(name);
                if (SENSITIVE_KEYS.contains(name.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT))) {
                    objectNode.put(name, "***");
                } else {
                    maskSensitiveValues(value);
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::maskSensitiveValues);
        }
    }

    /** 로그 필드에 null 대신 빈 문자열을 사용한다. */
    private String nullToEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }

    /** 쿼리 파라미터를 JSON으로 만들고 민감한 값은 마스킹한다. */
    private JsonNode queryNode(HttpServletRequest request) {
        if (!StringUtils.hasText(request.getQueryString())) return objectMapper.nullNode();
        ObjectNode query = objectMapper.createObjectNode();
        request.getParameterMap().forEach((name, values) -> {
            if (SENSITIVE_KEYS.contains(name.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT))) {
                query.put(name, "***");
            } else if (values.length == 1) {
                query.put(name, values[0]);
            } else {
                ArrayNode array = query.putArray(name);
                for (String value : values) array.add(value);
            }
        });
        return query;
    }

    /** 유효한 요청 추적 ID를 재사용하고, 없으면 새 ID를 생성한다. */
    private String traceId(HttpServletRequest request) {
        var incoming = request.getHeader(TRACE_ID_HEADER);
        return StringUtils.hasText(incoming) && incoming.matches("[A-Za-z0-9_-]{8,128}")
                ? incoming
                : UUID.randomUUID().toString().replace("-", "");
    }
}

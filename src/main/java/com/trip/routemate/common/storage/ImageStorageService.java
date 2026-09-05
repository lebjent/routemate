package com.trip.routemate.common.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/** 공통 NAS 이미지 저장과 DB 경로 조회를 담당한다. */
@Service
@RequiredArgsConstructor
public class ImageStorageService {
    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final Map<String, String> TYPES = Map.of("png", "image/png", "jpeg", "image/jpeg", "gif", "image/gif");
    private final LocalStorageProperties properties;
    private final StoredImageRepository repository;

    public record UploadedImage(String imageId, String imageUrl) {}
    public record ImageFile(Resource resource, String contentType, long size) {}

    public UploadedImage store(ImageCategory category, MultipartFile file) {
        if (file == null || file.isEmpty()) throw badRequest("이미지 파일을 선택하세요.");
        if (file.getSize() > MAX_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 10MB 이하만 업로드할 수 있습니다.");
        Path target = null;
        try {
            byte[] bytes;
            try (var input = file.getInputStream()) { bytes = input.readNBytes(MAX_BYTES + 1); }
            if (bytes.length > MAX_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 10MB 이하만 업로드할 수 있습니다.");
            var type = detectType(bytes);
            var id = UUID.randomUUID().toString();
            var folder = switch (category) {
                case PRODUCT -> properties.optionProductDirectory();
                case TRAVEL_PLAN -> properties.travelPlanDirectory();
                default -> category.directory();
            };
            var relative = folder + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + id + "." + type.substring(6).replace("jpeg", "jpg");
            var root = Path.of(properties.rootDirectory()).toAbsolutePath().normalize();
            var destination = root.resolve(relative).normalize();
            if (!destination.startsWith(root)) throw badRequest("올바르지 않은 저장 경로입니다.");
            Files.createDirectories(destination.getParent());
            if (!destination.getParent().toRealPath().startsWith(root.toRealPath())) throw badRequest("올바르지 않은 저장 경로입니다.");
            Files.write(destination, bytes, java.nio.file.StandardOpenOption.CREATE_NEW);
            target = destination;
            repository.saveAndFlush(new StoredImage(id, category, relative, type, bytes.length));
            return new UploadedImage(id, "/api/public/images/" + id);
        } catch (IOException | RuntimeException exception) {
            if (target != null) {
                try { Files.deleteIfExists(target); } catch (IOException cleanup) { exception.addSuppressed(cleanup); }
            }
            if (exception instanceof RuntimeException runtime) throw runtime;
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일을 저장하지 못했습니다.", exception);
        }
    }

    public ImageFile load(String imageId) {
        var image = repository.findById(imageId).orElseThrow(ImageStorageService::notFound);
        try {
            var root = Path.of(properties.rootDirectory()).toRealPath();
            var path = root.resolve(image.getRelativePath()).normalize();
            if (!path.startsWith(root) || !path.toRealPath().startsWith(root) || !Files.isRegularFile(path)) throw notFound();
            return new ImageFile(new FileSystemResource(path), image.getContentType(), Files.size(path));
        } catch (IOException exception) { throw notFound(); }
    }

    /** 여행 일정 이미지는 로그인한 사용자만 조회할 수 있도록 최소 접근 정책을 적용한다. */
    public ImageFile load(String imageId, Authentication authentication) {
        var image = repository.findById(imageId).orElseThrow(ImageStorageService::notFound);
        if (image.getCategory() == ImageCategory.TRAVEL_PLAN
                && (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return load(imageId);
    }

    private String detectType(byte[] bytes) throws IOException {
        // WebP는 기본 JDK 디코더가 없어 RIFF 컨테이너와 이미지 청크를 확인한다.
        if (bytes.length >= 30 && ascii(bytes, 0, 4).equals("RIFF") && ascii(bytes, 8, 4).equals("WEBP")
                && java.util.Set.of("VP8 ", "VP8L", "VP8X").contains(ascii(bytes, 12, 4))) {
            long declared = Integer.toUnsignedLong(java.nio.ByteBuffer.wrap(bytes, 4, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt());
            if (declared + 8 == bytes.length) return "image/webp";
        }
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (readers.hasNext()) {
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    var type = TYPES.get(reader.getFormatName().toLowerCase(java.util.Locale.ROOT));
                    if (type != null && reader.getWidth(0) > 0 && reader.getHeight(0) > 0) return type;
                } finally { reader.dispose(); }
            }
        } catch (IOException exception) { throw badRequest("이미지 파일을 읽을 수 없습니다."); }
        throw badRequest("JPG, PNG, WEBP, GIF 이미지 파일만 업로드할 수 있습니다.");
    }

    private static String ascii(byte[] bytes, int offset, int length) { return new String(bytes, offset, length, java.nio.charset.StandardCharsets.US_ASCII); }
    private static ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."); }
}

package com.trip.routemate.common.concurrent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * 가상 스레드를 사용해 제한된 수의 독립 작업을 병렬 처리하는 공통 실행기다.
 *
 * 작업 결과는 입력 순서대로 반환한다. 최대 동시 실행 수를 제한하므로 외부 API, 파일 I/O처럼
 * 블로킹 작업을 병렬화하면서도 대상 시스템에 과도한 요청을 보내지 않는다.
 */
@Component
public class ParallelTaskExecutor {

    private final ParallelTaskProperties properties;

    public ParallelTaskExecutor(ParallelTaskProperties properties) {
        this.properties = properties;
    }

    /** 설정된 최대 동시 실행 수로 작업 목록을 병렬 실행한다. */
    public <T> List<T> invokeAll(List<? extends Callable<T>> tasks) {
        return invokeAll(tasks, properties.maxThreads());
    }

    /**
     * 지정한 최대 동시 실행 수로 작업 목록을 병렬 실행한다.
     *
     * @param tasks 실행할 작업 목록
     * @param maxThreads 동시에 실행할 최대 작업 수
     * @param <T> 각 작업의 응답 유형
     * @return 입력 작업 순서를 유지한 실행 결과
     */
    public <T> List<T> invokeAll(List<? extends Callable<T>> tasks, int maxThreads) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        var concurrency = Math.min(validateMaxThreads(maxThreads), tasks.size());
        try (var executor = Executors.newFixedThreadPool(concurrency, Thread.ofVirtual().name("routemate-parallel-", 0).factory())) {
            var futures = executor.invokeAll(tasks);
            return collectResults(futures);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("병렬 작업 실행이 중단되었습니다.", exception);
        }
    }

    /**
     * 입력 목록의 각 요소를 같은 변환 함수로 병렬 처리한다.
     *
     * @param inputs 변환할 입력 목록
     * @param mapper 입력 하나를 응답 하나로 변환하는 작업
     * @param <I> 입력 유형
     * @param <O> 응답 유형
     * @return 입력 순서를 유지한 변환 결과
     */
    public <I, O> List<O> map(List<I> inputs, Function<? super I, ? extends O> mapper) {
        var tasks = inputs.stream()
                .<Callable<O>>map(input -> () -> mapper.apply(input))
                .toList();
        return invokeAll(tasks);
    }

    /** 호출별로 별도 동시 실행 수가 필요할 때 사용하는 map 변형이다. */
    public <I, O> List<O> map(List<I> inputs, Function<? super I, ? extends O> mapper, int maxThreads) {
        var tasks = inputs.stream()
                .<Callable<O>>map(input -> () -> mapper.apply(input))
                .toList();
        return invokeAll(tasks, maxThreads);
    }

    /** Future 결과를 입력 순서대로 꺼내고, 작업의 원래 RuntimeException은 보존한다. */
    private <T> List<T> collectResults(List<Future<T>> futures) {
        var results = new ArrayList<T>(futures.size());
        for (var future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("병렬 작업 결과 대기가 중단되었습니다.", exception);
            } catch (ExecutionException exception) {
                throw propagate(exception.getCause());
            }
        }
        return List.copyOf(results);
    }

    /** 잘못된 호출 설정을 즉시 드러내고, 작업 내부의 런타임 예외는 호출자에게 그대로 전달한다. */
    private int validateMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("최대 병렬 실행 수는 1 이상이어야 합니다.");
        }
        return maxThreads;
    }

    private RuntimeException propagate(Throwable cause) {
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new IllegalStateException("병렬 작업 실행에 실패했습니다.", cause);
    }
}

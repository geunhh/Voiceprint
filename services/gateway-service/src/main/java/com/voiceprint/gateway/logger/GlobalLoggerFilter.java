package com.voiceprint.gateway.logger;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class GlobalLoggerFilter extends AbstractGatewayFilterFactory<GlobalLoggerFilter.Config> {

    private static final String TRACE_HEADER = "X-Trace-Id";

    public GlobalLoggerFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest originalRequest = exchange.getRequest();

            // 1) 들어온 요청의 X-Trace-Id 헤더를 확인
            String traceId = originalRequest.getHeaders().getFirst(TRACE_HEADER);

            // 2) 클라이언트가 보내지 않았다면 Gateway가 새로 생성
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().substring(0, 8);
            }

            // 3) downstream 으로 넘길 요청에 X-Trace-Id 를 항상 세팅
            ServerHttpRequest mutatedRequest = originalRequest.mutate()
                    .header(TRACE_HEADER, traceId)
                    .build();

            // 4) 교체한 request 로 exchange 재구성
            var mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            // ---- Request 로그 ----
            if (config.isPreLogger()) {
                try {
                    MDC.put("traceId", traceId);
                    log.info("Gateway Request: method={}, path={}, remote={}",
                            mutatedRequest.getMethod(),
                            mutatedRequest.getURI().getPath(),
                            mutatedRequest.getRemoteAddress());
                } finally {
                    MDC.clear();
                }
            }

            String finalTraceId = traceId;
            // ---- Filter 체인 + Response 로그 ----
            return chain.filter(mutatedExchange)
                    .then(Mono.fromRunnable(() -> {
                        if (config.isPostLogger()) {
                            ServerHttpResponse response = mutatedExchange.getResponse();
                            try {
                                MDC.put("traceId", finalTraceId);
                                log.info("Gateway Response: status={}", response.getStatusCode());
                            } finally {
                                MDC.clear();
                            }
                        }
                    }));
        };
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
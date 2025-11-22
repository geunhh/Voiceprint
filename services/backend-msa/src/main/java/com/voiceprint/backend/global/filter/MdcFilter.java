package com.voiceprint.backend.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 요청마다 traceId를 생성해서 MDC에 넣어주는 필터.
 * - OncePerRequestFilter 를 상속했기 때문에 한 요청당 한 번만 실행됨.
 * - 이후 모든 로그에서 %X{traceId} 로 같은 traceId를 확인할 수 있다.
 */
@Component
public class MdcFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String TRACE_HEADER = "X-Trace-Id";

    /**
     * 실제 필터 로직.
     * 요청이 들어올 때 traceId를 MDC에 넣고,
     * 체인 이후 finally 에서 MDC를 정리(cleanup) 해준다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1) 먼저 헤더에서 traceId를 찾는다 (서비스 간 전파용)
            String traceId = request.getHeader(TRACE_HEADER);

            // 2) 헤더에 없으면 MDC에서 한 번 더 확인 (이미 세팅된 경우)
            if (traceId == null || traceId.isBlank()) {
                traceId = MDC.get(TRACE_ID);
            }

            // 3) 그래도 없으면 새로 생성 (이 서비스가 최초 진입점일 수도 있으니까)
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().substring(0, 8);
            }
            // 4) put.
            MDC.put(TRACE_ID, traceId);

            // 응답 헤더에도 넣어줌.
            response.setHeader(TRACE_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리가 끝나면 MDC를 반드시 비워줘야 함.
            // MDC는 내부적으로 ThreadLocal을 사용하기 때문에,
            // 스레드풀 환경에서는 이전 요청의 값이 다음 요청에 섞이지 않도록 정리 필수.
            MDC.clear();
        }
    }
}

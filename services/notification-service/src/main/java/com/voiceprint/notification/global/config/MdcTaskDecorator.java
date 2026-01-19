package com.voiceprint.notification.global.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        // 현재 스레드의 MDC 컨텍스트 복사 (traceId 포함)
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            // 실핸 전, 이전 값을 백업해두었다가
            Map<String, String> previous = MDC.getCopyOfContextMap();

            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                } else {
                    // 부모에 MDC가 없을 경우, 자식에서도 [traceId =] 이게 맞다.
                    MDC.clear();
                }
                // 실제 작업 실행
                runnable.run();
            } finally {
                // Runnable 단위의 clear()
                MDC.clear();
            }
        };
    }
}

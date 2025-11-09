package com.voiceprint.notification.adapter.out.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SessionStatusReader {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_KEY_PREFIX = "session";

    /**
     * 단일 유저 status 조회 (기존 방식 그대로)
     */
    public String getStatus(Integer userId) {
        String sessionKey = SESSION_KEY_PREFIX + ":" + userId;
        Object statusObj = redisTemplate.opsForHash().get(sessionKey, "status");
        return (statusObj != null)
                ? statusObj.toString().replace("\"", "")
                : "NOT_EXIST";
    }

    /**
     * 여러 userId에 대해 status를 한 번의 pipeline으로 가져오는 메서드
     */
    public Map<Integer, String> getStatusesWithPipeline(List<Integer> userIds) {
        List<Object> rawResults = redisTemplate.executePipelined(
                // pipeline 안에서 실행할 명령을 정의하는 콜백
                new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        // 이 안의 모든 명령이 한 "파이프라인 세션"으로 묶임
                        for (Integer userId : userIds) {
                            String key = SESSION_KEY_PREFIX + ":" + userId;
                            operations.opsForHash().get(key, "status");
                        }
                        return null; // 결과는 executePipelined가 감싼 리스트로 반환
                    }
                }
        );

        Map<Integer, String> result = new HashMap<>(userIds.size());
        for (int i = 0; i < userIds.size(); i++) {
            Integer userId = userIds.get(i);
            Object raw = rawResults.get(i);

            String status = (raw != null)
                    ? raw.toString().replace("\"", "")
                    : "NOT_EXIST";

            result.put(userId, status);
        }
        return result;
    }
}

package com.voiceprint.notification.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 알림 생성 및 전송 중
 * redis, MySQL에 쓰이는 시간.
 */
@Component
@Slf4j
public class NotificationPerfMonitor {

    private final AtomicLong redisGetNanos = new AtomicLong();
    private final AtomicLong redisPublishNanos = new AtomicLong();
    private final AtomicLong dbReadNanos = new AtomicLong();
    private final AtomicLong dbInsertNanos = new AtomicLong();

    //reset 메서드
    public void reset() {
        redisGetNanos.set(0L);
        redisPublishNanos.set(0L);
        dbReadNanos.set(0L);
        dbInsertNanos.set(0L);
    }

    public void addRedisGet(long nanos) {
        redisGetNanos.addAndGet(nanos);
    }

    public void addRedisPublish(long nanos) {
        redisPublishNanos.addAndGet(nanos);
    }

    public void addDbRead(long nanos) {
        dbReadNanos.addAndGet(nanos);
    }

    public void addDbInsert(long nanos) {
        dbInsertNanos.addAndGet(nanos);
    }

    public void logSummary(String tag, long totalMs, int totalUsers, int sent, int skipped) {
        long redisGetMs   = redisGetNanos.get()      / 1_000_000L;
        long redisPubMs   = redisPublishNanos.get()  / 1_000_000L;
        long dbReadMs     = dbReadNanos.get()        / 1_000_000L;
        long dbInsertMs   = dbInsertNanos.get()      / 1_000_000L;

        double base = totalMs > 0 ? totalMs : 1.0;

        double rGetPct = redisGetMs  * 100.0 / base;
        double rPubPct = redisPubMs  * 100.0 / base;
        double dReadPct = dbReadMs   * 100.0 / base;
        double dInsPct = dbInsertMs  * 100.0 / base;

        log.info(
                "[PERF][{}] total={}ms | users={} | sent={} | skipped={} | " +
                        "redis_get={}ms({}%) | redis_publish={}ms({}%) | db_read={}ms({}%) | db_insert={}ms({}%)",
                tag,
                totalMs, totalUsers, sent, skipped,
                redisGetMs,  String.format("%.1f", rGetPct),
                redisPubMs,  String.format("%.1f", rPubPct),
                dbReadMs,    String.format("%.1f", dReadPct),
                dbInsertMs,  String.format("%.1f", dInsPct)
        );
    }
}

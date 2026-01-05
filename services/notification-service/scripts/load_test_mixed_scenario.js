import http from 'k6/http';
import { check, sleep } from 'k6';

const SECRET_KEY = __ENV.LOADTEST_SECRET || 'voiceprint-loadtest-secret';
const BASE_URL = __ENV.TARGET_URL || 'http://localhost:81';

export let options = {
    scenarios: {
        // SSE 연결: 지속적인 롱폴링 연결
        sse_connections: {
            executor: 'ramping-vus',
            exec: 'sseConnection',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 1000 },   // 1분간 2K SSE 연결
                { duration: '3m', target: 1000 },   // 3분간 2K 유지
                { duration: '1m', target: 0 },      // 종료
            ],
            gracefulRampDown: '30s',
        },

        // 알림 조회: 주기적인 API 호출
        notification_query: {
            executor: 'ramping-vus',
            exec: 'notificationQuery',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 500 },   // 1분간 1K 조회
                { duration: '3m', target: 1000 },   // 3분간 2K 조회
                { duration: '1m', target: 0 },      // 종료
            ],
            gracefulRampDown: '30s',
        },
    },
};

// SSE 연결 시나리오
export function sseConnection() {
    const userId = __VU;
    const token = `Bearer ${SECRET_KEY}-${userId}`;

    const params = {
        headers: {
            'Authorization': token,
            'Accept': 'text/event-stream',
        },
        timeout: '120s',  // SSE는 긴 타임아웃
        tags: { scenario: 'sse_connections' },
    };

    const res = http.get(`${BASE_URL}/api/notifications/subscribe`, params);

    check(res, {
        'SSE: status is 200': (r) => r.status === 200,
        'SSE: content-type is text/event-stream': (r) =>
            r.headers['Content-Type'] && r.headers['Content-Type'].includes('text/event-stream'),
    });

    // SSE는 긴 연결 유지 시뮬레이션
    sleep(120);
}

// 알림 조회 시나리오
export function notificationQuery() {
    const userId = (__VU % 1000000) + 1; // 1~100만 순환
    const cursor = Math.random() > 0.5 ? Math.floor(Math.random() * 27000000) : null;

    const params = {
        headers: {
            'Authorization': `Bearer ${SECRET_KEY}-${userId}`,
            'Content-Type': 'application/json',
        },
        tags: { scenario: 'notification_query' },
    };

    const url = cursor
        ? `${BASE_URL}/api/notifications?cursor=${cursor}&size=20`
        : `${BASE_URL}/api/notifications?size=20`;

    const res = http.get(url, params);

    check(res, {
        'Query: status is 200': (r) => r.status === 200,
        'Query: response time < 500ms': (r) => r.timings.duration < 500,
        'Query: has data with diaries': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data && Array.isArray(body.data.diaries);
            } catch {
                return false;
            }
        },
    });

    // 사용자 행동 시뮬레이션 (2~5초 대기)
    sleep(Math.random() * 3 + 2);
}

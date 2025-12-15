import k6 from 'k6';
import { check, sleep } from 'k6';
import http from 'k6/http';

export let options = {
    scenarios: {
        sse_connection: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '5m', target: 5000 },
                { duration: '10m', target: 5000 },
                { duration: '5m', target: 0 },
            ],
            gracefulRampDown: '30s',
        },
    },
};

const SECRET_KEY = __ENV.LOADTEST_SECRET || 'voiceprint-loadtest-secret';
const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8001';

export default function () {
    // 각 VU(Virtual User)마다 고유한 User ID 생성 (Math.random 사용이 더 안전할 수 있으나 __VU도 무방)
    let userId = __VU;
    let token = `Bearer ${SECRET_KEY}-${userId}`;

    const params = {
        headers: {
            'Authorization': token,
            'Accept': 'text/event-stream',
        },
        timeout: '600s', // Long timeout for SSE
    };

    // SSE 연결 요청
    const res = http.get(`${BASE_URL}/api/notifications/subscribe`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'content-type is text/event-stream': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('text/event-stream'),
    });

    // 연결 유지 시뮬레이션
    sleep(600);
}

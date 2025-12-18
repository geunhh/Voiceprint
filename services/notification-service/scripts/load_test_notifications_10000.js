import k6 from 'k6';
import { check, sleep } from 'k6';
import http from 'k6/http';



export let options = {
    scenarios: {
        notification_query: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 10000 },  // 2분간 10,000까지 증가 (83 VU/sec)
                { duration: '3m', target: 10000 }, // 10분간 10,000 유지
                { duration: '1m', target: 0 },      // 2분간 종료
            ],
            gracefulRampDown: '30s',
        },
    },

    timeout: '120s',
};

const SECRET_KEY = __ENV.LOADTEST_SECRET || 'voiceprint-loadtest-secret';
const BASE_URL = __ENV.TARGET_URL || 'http://localhost:81';


export default function () {
    const userId = (__VU % 1000000) + 1; // 1~100만 순환
    const cursor = Math.random() > 0.5 ? Math.floor(Math.random() * 27000000) : null;

    const params = {
        headers: {
            'Authorization': `Bearer ${SECRET_KEY}-${userId}`,
            'Content-Type': 'application/json',
        },
    };

    // 알림 조회 API 호출
    const url = cursor
        ? `${BASE_URL}/api/notifications?cursor=${cursor}&size=20`
        : `${BASE_URL}/api/notifications?size=20`;

    const res = http.get(url, params);

    const checkRes = check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 100ms': (r) => r.timings.duration < 100,
        'has notifications field': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.data && Array.isArray(body.data.diaries);
            } catch {
                return false;
            }
        },

    })


    // 사용자 행동 시뮬레이션 (1~3초 대기)
    sleep(Math.random() * 2 + 1);
}

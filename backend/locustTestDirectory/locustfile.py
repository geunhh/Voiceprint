from locust import HttpUser, task, between
import random

# 실제 유저 4명의 토큰 (Bearer 포함)
TOKENS = [
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA2MDU0NjA1Mjk0NjUxNTUyNzM2IiwiaWF0IjoxNzQ4MDY5ODc4LCJleHAiOjE3NDg2Njk4Nzh9.4yuEonzvRGQ3YYnYZI10x4hAnIwAh9l8LaDJe76z5dQ",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTE3MTYyMTUwMjM0ODg2Njc3MjY0IiwiaWF0IjoxNzQ4MDY5OTEyLCJleHAiOjE3NDg2Njk5MTJ9.jPCX3BICkLfk8M1-poj1-9M669ZATd_pZuHhs_EvmNI",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTE2NTk3NDg3OTI1NjA3MzcyNTkxIiwiaWF0IjoxNzQ4MDY5OTQzLCJleHAiOjE3NDg2Njk5NDN9._Q5JruNoO8b9IbyddGbMrb8qp85Y6Jl2x6R7NRD5tis",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA3MzYxMzg0NjEzMTk1NzM3MjExIiwiaWF0IjoxNzQ4MDcwMDE5LCJleHAiOjE3NDg2NzAwMTl9.SecHc9uFNgii50GPUg7-0a0jIzPOmj_kRMa6SPaMgf8",
]

class NotificationTestUser(HttpUser):
    wait_time = between(0.5, 1)

    def on_start(self):
        self.token = random.choice(TOKENS)

    @task(1)
    def test_paged_notification(self):
        
        self.client.get(
            "/api/notifications?size=10",
            headers={"Authorization": self.token}
        )

    @task(1)
    def test_all_unpaged_notification(self):
        # 전체 알림 조회
        self.client.get(
            "/api/notifications/all-unpaged",
            headers={"Authorization": self.token}
        )
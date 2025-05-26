from locust import HttpUser, task, between
import random

# 실제 유저 4명의 토큰 (Bearer 포함)
TOKENS = [
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA2MDU0NjA1Mjk0NjUxNTUyNzM2IiwiaWF0IjoxNzQ4MDY5ODc4LCJleHAiOjE3NDg2Njk4Nzh9.4yuEonzvRGQ3YYnYZI10x4hAnIwAh9l8LaDJe76z5dQ",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTE3MTYyMTUwMjM0ODg2Njc3MjY0IiwiaWF0IjoxNzQ4MDY5OTEyLCJleHAiOjE3NDg2Njk5MTJ9.jPCX3BICkLfk8M1-poj1-9M669ZATd_pZuHhs_EvmNI",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTE2NTk3NDg3OTI1NjA3MzcyNTkxIiwiaWF0IjoxNzQ4MDY5OTQzLCJleHAiOjE3NDg2Njk5NDN9._Q5JruNoO8b9IbyddGbMrb8qp85Y6Jl2x6R7NRD5tis",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA3MzYxMzg0NjEzMTk1NzM3MjExIiwiaWF0IjoxNzQ4MDcwMDE5LCJleHAiOjE3NDg2NzAwMTl9.SecHc9uFNgii50GPUg7-0a0jIzPOmj_kRMa6SPaMgf8",
]

class DiaryLoadTest(HttpUser):
    wait_time = between(1, 2)  # 사용자당 요청 간격 (초)

    @task
    def fetch_diaries(self):
        token = random.choice(TOKENS)
        cursor = random.choice([None, 300, 250, 200])  # 랜덤 커서 (None은 첫 페이지)

        params = {
            "cursor": cursor,
            "size": 10
        }

        with self.client.get(
            "/api/diaries/me/all",
            headers={"Authorization": token},
            params=params,
            name="/api/diaries/me/all",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Failed with {response.status_code}")
from locust import HttpUser, task, between
import random

# 실제 유저 4명의 토큰 (Bearer 포함)
TOKENS = [
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA2MDU0NjA1Mjk0NjUxNTUyNzM2IiwiaWF0IjoxNzQ4NTE0Mzg1LCJleHAiOjE3NDkxMTQzODV9.NRK5F5wr20hlaNFC6QsOGK3D8u0tqZTu2z7Y4CPqFi4",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA3MzYxMzg0NjEzMTk1NzM3MjExIiwiaWF0IjoxNzQ4NTE0NDE0LCJleHAiOjE3NDkxMTQ0MTR9.WnDCQ-9PFWVwB5u9BK4xeW4R3uoRNe70DRjB2XoHF40",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTE3MTYyMTUwMjM0ODg2Njc3MjY0IiwiaWF0IjoxNzQ4NTEzMTMzLCJleHAiOjE3NDkxMTMxMzN9.ev6rZRNpsc3WrkGHXc6a30b-GBpaoF_a_NoZP5s9rC0",
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTE2NTk3NDg3OTI1NjA3MzcyNTkxIiwiaWF0IjoxNzQ4NTEzMzQyLCJleHAiOjE3NDkxMTMzNDJ9.Zs1KLdP1P7z2HqsbVeGuKGge8Csh0vDHaIGj3BUMCTU"
]

class DiaryLoadTest(HttpUser):
    wait_time = between(1, 2)  # 사용자당 요청 간격 (초)

    @task
    def fetch_diaries(self):
        token = random.choice(TOKENS)
        cursor = random.choice([None] + [random.randint(1000, 999000) for _ in range(5)])

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
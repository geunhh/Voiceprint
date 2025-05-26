from locust import HttpUser, task, between
import random

# 실제 유저 4명의 토큰 (Bearer 포함)
TOKENS = [
    "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhY2Nlc3MiLCJwcm92aWRlcklkIjoiMTA2MDU0NjA1Mjk0NjUxNTUyNzM2IiwiaWF0IjoxNzQ4MDY5ODc4LCJleHAiOjE3NDg2Njk4Nzh9.4yuEonzvRGQ3YYnYZI10x4hAnIwAh9l8LaDJe76z5dQ",
]

class NotificationTestUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.token = random.choice(TOKENS)

    
    @task
    def trigger_scheduler(self):
        self.client.post("/test/scheduler/trigger",
                         
            headers={"Authorization": self.token})
    
        self.stop(True)


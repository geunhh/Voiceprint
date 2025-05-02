from typing import List, Dict
from pydantic import BaseModel

class Message(BaseModel):
    role: str
    content: str

class Chat(BaseModel):
    message: List[Message]

class MyChat(BaseModel):
    mychat: str
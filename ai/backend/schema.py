from typing import List, Dict
from pydantic import BaseModel

class Message(BaseModel):
    role: str
    content: str

class Chat(BaseModel):
    message: List[Message]

class ChatResponse(BaseModel) :
    userid : int
    chatting : str


class MyChat(BaseModel):
    # mychat: str
    userid : int

class PromtTest(BaseModel) :
    prev_diary : str

class ChatSaveTest(BaseModel) :
    user_id : int
    chat_history : list


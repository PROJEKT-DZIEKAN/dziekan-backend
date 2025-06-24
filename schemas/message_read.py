from pydantic import BaseModel
from datetime import datetime

class MessageRead(BaseModel):
    id: int
    sender_id: int
    receiver_id: int
    content: str
    timestamp: datetime
    read: bool

    class Config:
        orm_mode = True

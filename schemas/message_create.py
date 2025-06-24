from pydantic import BaseModel

class MessageCreate(BaseModel):
    receiver_id: int
    content: str

import asyncio
import json
from datetime import datetime
from typing import Dict, List
from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends, HTTPException, status
from sqlalchemy.orm import Session
from db import get_db
from app.models.user import User
from app.models.message import ChatRoom, ChatMessage
from app.auth import get_current_user, get_current_admin_user
from app.schemas.chat import ChatMessageResponse, ChatRoomResponse
import logging

router = APIRouter(prefix="/chat", tags=["chat"])

class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[int, WebSocket] = {}
        self.user_rooms: Dict[int, int] = {}  # user_id -> room_id
        self.admin_connections: List[WebSocket] = []
        self.admin_users: Dict[WebSocket, int] = {}  # websocket -> admin_id

    async def connect_user(self, websocket: WebSocket, user_id: int, room_id: int):
        await websocket.accept()
        self.active_connections[user_id] = websocket
        self.user_rooms[user_id] = room_id

    async def connect_admin(self, websocket: WebSocket, admin_id: int):
        await websocket.accept()
        self.admin_connections.append(websocket)
        self.admin_users[websocket] = admin_id

    def disconnect_user(self, user_id: int):
        if user_id in self.active_connections:
            del self.active_connections[user_id]
        if user_id in self.user_rooms:
            del self.user_rooms[user_id]

    def disconnect_admin(self, websocket: WebSocket):
        if websocket in self.admin_connections:
            self.admin_connections.remove(websocket)
        if websocket in self.admin_users:
            del self.admin_users[websocket]

    async def send_to_user(self, user_id: int, message: dict):
        if user_id in self.active_connections:
            try:
                await self.active_connections[user_id].send_text(json.dumps(message))
            except Exception as e:
                logging.error(f"Error sending to user {user_id}: {e}")

    async def send_to_admins(self, message: dict, exclude_admin: int = None):
        for websocket in self.admin_connections[:]:  # Create copy to avoid modification during iteration
            try:
                admin_id = self.admin_users.get(websocket)
                if admin_id != exclude_admin:
                    await websocket.send_text(json.dumps(message))
            except Exception as e:
                logging.error(f"Error sending to admin: {e}")
                self.disconnect_admin(websocket)

manager = ConnectionManager()

def get_or_create_chat_room(db: Session, user_id: int) -> ChatRoom:
    """Get existing chat room or create new one for user"""
    room = db.query(ChatRoom).filter(
        ChatRoom.user_id == user_id,
        ChatRoom.is_active == True
    ).first()
    
    if not room:
        room = ChatRoom(user_id=user_id, is_active=True)
        db.add(room)
        db.commit()
        db.refresh(room)
    
    return room

def save_message(db: Session, room_id: int, sender_id: int, message: str, sender_type: str) -> ChatMessage:
    """Save message to database"""
    chat_message = ChatMessage(
        room_id=room_id,
        sender_id=sender_id,
        message=message,
        sender_type=sender_type
    )
    db.add(chat_message)
    db.commit()
    db.refresh(chat_message)
    return chat_message

@router.websocket("/ws/user")
async def websocket_user_endpoint(
    websocket: WebSocket,
    token: str,
    db: Session = Depends(get_db)
):
    try:
        # Verify token manually since WebSocket doesn't support Depends with auth
        from app.auth import verify_access_token
        user_id = verify_access_token(token)
        
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            await websocket.close(code=1008, reason="User not found")
            return

        # Get or create chat room
        room = get_or_create_chat_room(db, user_id)
        await manager.connect_user(websocket, user_id, room.id)
        
        # Send chat history
        messages = db.query(ChatMessage).filter(
            ChatMessage.room_id == room.id
        ).order_by(ChatMessage.created_at.desc()).limit(50).all()
        
        await websocket.send_text(json.dumps({
            "type": "history",
            "messages": [
                {
                    "id": msg.id,
                    "sender_type": msg.sender_type,
                    "message": msg.message,
                    "created_at": msg.created_at.isoformat(),
                    "sender_name": msg.sender.username if msg.sender else "Unknown"
                }
                for msg in reversed(messages)
            ]
        }))
        
        # Notify admins about new user connection
        await manager.send_to_admins({
            "type": "user_connected",
            "user_id": user_id,
            "username": user.username,
            "room_id": room.id,
            "timestamp": datetime.now().isoformat()
        })
        
        while True:
            data = await websocket.receive_text()
            message_data = json.loads(data)
            
            if message_data.get("type") == "message":
                message_text = message_data.get("message", "").strip()
                if not message_text:
                    continue
                
                # Save message to database
                chat_message = save_message(db, room.id, user_id, message_text, "user")
                
                # Send to admins
                await manager.send_to_admins({
                    "type": "user_message",
                    "room_id": room.id,
                    "user_id": user_id,
                    "username": user.username,
                    "message": message_text,
                    "message_id": chat_message.id,
                    "timestamp": chat_message.created_at.isoformat()
                })
                
    except WebSocketDisconnect:
        manager.disconnect_user(user_id)
        await manager.send_to_admins({
            "type": "user_disconnected",
            "user_id": user_id,
            "timestamp": datetime.now().isoformat()
        })
    except Exception as e:
        logging.error(f"WebSocket error for user {user_id}: {e}")
        await websocket.close(code=1008)

@router.websocket("/ws/admin")
async def websocket_admin_endpoint(
    websocket: WebSocket,
    token: str,
    db: Session = Depends(get_db)
):
    try:
        # Verify admin token
        from app.auth import verify_access_token
        admin_id = verify_access_token(token)
        
        admin = db.query(User).filter(
            User.id == admin_id,
            User.is_admin == True
        ).first()
        
        if not admin:
            await websocket.close(code=1008, reason="Admin access required")
            return

        await manager.connect_admin(websocket, admin_id)
        
        # Send active rooms
        active_rooms = db.query(ChatRoom).filter(ChatRoom.is_active == True).all()
        await websocket.send_text(json.dumps({
            "type": "active_rooms",
            "rooms": [
                {
                    "id": room.id,
                    "user_id": room.user_id,
                    "username": room.user.username,
                    "created_at": room.created_at.isoformat(),
                    "unread_count": db.query(ChatMessage).filter(
                        ChatMessage.room_id == room.id,
                        ChatMessage.sender_type == "user",
                        ChatMessage.is_read == False
                    ).count()
                }
                for room in active_rooms
            ]
        }))
        
        while True:
            data = await websocket.receive_text()
            message_data = json.loads(data)
            
            if message_data.get("type") == "message":
                room_id = message_data.get("room_id")
                message_text = message_data.get("message", "").strip()
                
                if not room_id or not message_text:
                    continue
                
                # Verify room exists
                room = db.query(ChatRoom).filter(ChatRoom.id == room_id).first()
                if not room:
                    continue
                
                # Save message
                chat_message = save_message(db, room_id, admin_id, message_text, "admin")
                
                # Mark user messages as read
                db.query(ChatMessage).filter(
                    ChatMessage.room_id == room_id,
                    ChatMessage.sender_type == "user",
                    ChatMessage.is_read == False
                ).update({"is_read": True})
                db.commit()
                
                # Send to user
                await manager.send_to_user(room.user_id, {
                    "type": "admin_message",
                    "admin_id": admin_id,
                    "admin_name": admin.username,
                    "message": message_text,
                    "message_id": chat_message.id,
                    "timestamp": chat_message.created_at.isoformat()
                })
                
                # Notify other admins
                await manager.send_to_admins({
                    "type": "admin_response",
                    "room_id": room_id,
                    "admin_name": admin.username,
                    "message": message_text,
                    "timestamp": chat_message.created_at.isoformat()
                }, exclude_admin=admin_id)
                                
    except WebSocketDisconnect:
        manager.disconnect_admin(websocket)
    except Exception as e:
        logging.error(f"WebSocket error for admin {admin_id}: {e}")
        await websocket.close(code=1008)

@router.get("/rooms", response_model=List[ChatRoomResponse])
async def get_chat_rooms(
    current_user: User = Depends(get_current_admin_user),
    db: Session = Depends(get_db)
):
    """Get all active chat rooms (admin only)"""
    rooms = db.query(ChatRoom).filter(ChatRoom.is_active == True).all()
    return rooms

@router.get("/room/{room_id}/messages", response_model=List[ChatMessageResponse])
async def get_room_messages(
    room_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get messages for a specific room"""
    room = db.query(ChatRoom).filter(ChatRoom.id == room_id).first()
    if not room:
        raise HTTPException(status_code=404, detail="Room not found")
    
    # Check access permissions
    if not current_user.is_admin and room.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Access denied")
    
    messages = db.query(ChatMessage).filter(
        ChatMessage.room_id == room_id
    ).order_by(ChatMessage.created_at.asc()).all()
    
    return messages

@router.post("/room/{room_id}/mark-read")
async def mark_messages_read(
    room_id: int,
    current_user: User = Depends(get_current_admin_user),
    db: Session = Depends(get_db)
):
    """Mark all user messages in room as read (admin only)"""
    room = db.query(ChatRoom).filter(ChatRoom.id == room_id).first()
    if not room:
        raise HTTPException(status_code=404, detail="Room not found")
    
    db.query(ChatMessage).filter(
        ChatMessage.room_id == room_id,
        ChatMessage.sender_type == "user",
        ChatMessage.is_read == False
    ).update({"is_read": True})
    db.commit()
    
    return {"message": "Messages marked as read"}
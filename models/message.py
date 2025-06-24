from sqlalchemy import Boolean, Column, Integer, String, DateTime, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime

Base = declarative_base()

class Message(Base):
    __tablename__ = "messages"

    id = Column(Integer, primary_key=True, index=True)
    content = Column(String, nullable=False)
    sender_id = Column(Integer, nullable=False)
    receiver_id = Column(Integer, nullable=False)
    timestamp = Column(DateTime, default=datetime.now())

"""
Tutaj mozna dodac relacje z innymi tabelami, np. User, ale kurde ja bym to zachował niezależne od reszty bazy
bo coś sie spierdoli i no, a tak to jest i tyle, pozdrawiam cię Nicolas Bourbaki 
"""
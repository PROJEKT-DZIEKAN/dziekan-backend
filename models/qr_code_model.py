from pydantic import BaseModel, field_validator

class QRCodeConfig(BaseModel):
    data: str
    filename: str = 'MyQRCode.png'
    
    @field_validator('data')
    @classmethod
    def data_must_not_be_empty(cls, value):
        if not value or not value.strip():
            raise ValueError('Data cannot be empty')
        return value.strip()

    @field_validator('filename')
    @classmethod
    def filename_must_be_valid(cls, value):
        if not value.endswith('.png'):
            value += '.png'
        invalid_chars = '<>:"/\\|?*'
        for char in invalid_chars:
            value = value.replace(char, '_')
        return value
import qrcode
from pydantic import BaseModel, field_validator
import os

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


def generate_qr_code(data: str, filename: str = 'MyQRCode.png') -> str:
    config = QRCodeConfig(data=data, filename=filename)
    qr = qrcode.QRCode(
        version=3,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(config.data)
    qr.make(fit=True)
    
    img = qr.make_image(fill_color="black", back_color="white")
    img.save(config.filename)
    
    return os.path.abspath(config.filename)

# only for testing purposes
if __name__ == "__main__":
    try:
        path = generate_qr_code("https://www.youtube.com/watch?v=dQw4w9WgXcQ", "MyQRCode.png")
        print(f"QR Code generated and saved at: {path}")
    except ValueError as e:
        print(f"Error: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

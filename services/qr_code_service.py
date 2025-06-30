import qrcode
import os
from io import BytesIO
from models.qr_code_model import QRCodeConfig

def generate_qr_code(data: str, filename: str = 'MyQRCode.png') -> BytesIO:
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

    img_stream = BytesIO()
    img.save(img_stream, format='PNG')
    img_stream.seek(0)
    return img_stream
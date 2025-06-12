import qrcode
import os
from models.qr_code_model import QRCodeConfig

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

    output_path = os.path.join('output', config.filename)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    img.save(output_path)
    return output_path
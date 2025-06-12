import os
import pytest
import random
from services.qr_code_service import generate_qr_code
from models.qr_code_model import QRCodeConfig

def test_generate_qr_code_creates_file(tmp_path):
    test_data = "https://example.com"
    test_filename = tmp_path / "test_qr.png"

    result_path = generate_qr_code(test_data, str(test_filename))

    assert os.path.exists(result_path)
    assert result_path.endswith(".png")

def test_generate_qr_code_empty_data_raises():
    with pytest.raises(ValueError, match="Data cannot be empty"):
        generate_qr_code("  ")

def test_qrcode_config_strips_data():
    config = QRCodeConfig(data="  example  ", filename="qr.png")
    assert config.data == "example"

def test_qrcode_config_adds_png_extension():
    config = QRCodeConfig(data="test", filename="qr")
    assert config.filename.endswith(".png")

def test_qrcode_config_replaces_invalid_chars():
    config = QRCodeConfig(data="test", filename='qr<>:"/\\|?*.png')
    assert all(c not in config.filename for c in '<>:"/\\|?*')

def test_default_filename():
    config = QRCodeConfig(data="test")
    assert config.filename == "MyQRCode.png"

def test_generate_qr_code_with_random_data(tmp_path):
    random_data = f"https://example.com/{random.randint(1000, 9999)}"
    result_path = generate_qr_code(random_data, str(tmp_path / "random_qr.png"))

    assert os.path.exists(result_path)
    assert result_path.endswith(".png")
    assert os.path.getsize(result_path) > 0  

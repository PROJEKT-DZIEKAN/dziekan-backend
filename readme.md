# QR Code Generator Backend

A Python backend service for generating QR codes with customizable configurations.

## Features

- Generate QR codes from text data or URLs
- Customizable filename with automatic `.png` extension
- Automatic filename sanitization (removes invalid characters)
- Data validation and error handling
- Comprehensive test coverage

## Installation

1. Create and activate virtual environment:
```bash
python -m venv myenv
myenv\Scripts\activate
```

2. Install dependencies:
```bash
pip install qrcode[pil] pytest
```

## Usage

```python
from services.qr_code_service import generate_qr_code
from models.qr_code_model import QRCodeConfig

# Generate QR code with default settings
qr_path = generate_qr_code("https://example.com")

# Generate QR code with custom filename
qr_path = generate_qr_code("Hello World", "my_qr_code.png")

# Using QRCodeConfig for advanced configuration
config = QRCodeConfig(data="Sample text", filename="custom_qr")
# Automatically adds .png extension and sanitizes filename
```

## Testing

Run the test suite:
```bash
python -m pytest tests/test_qr_generator.py
```

## Project Structure

```
dziekan-backend/
├── services/
│   └── qr_code_service.py
├── models/
│   └── qr_code_model.py
├── tests/
│   └── test_qr_generator.py
└── myenv/
```

## API Reference

### `generate_qr_code(data, filename=None)`
Generates a QR code image file.

**Parameters:**
- `data` (str): Text or URL to encode
- `filename` (str, optional): Output filename

**Returns:** Path to generated QR code file

**Raises:** `ValueError` if data is empty

### `QRCodeConfig`
Configuration class for QR code generation.

**Attributes:**
- `data` (str): Data to encode (automatically stripped)
- `filename` (str): Output filename (auto-sanitized, .png added if missing)

## Best way to test and generate QR codes for testing

Someday we should create a working microservice, however today the best way to test an endpoint is via fast api docs :*
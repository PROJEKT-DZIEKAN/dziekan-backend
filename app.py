from fastapi import FastAPI, HTTPException, File, UploadFile
from fastapi.responses import FileResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import ValidationError
from models.qr_code_model import QRCodeConfig
from models.qr_code_model import QRRequest
from services.qr_code_service import generate_qr_code
from fastapi.responses import StreamingResponse
import uvicorn

app = FastAPI(title="QR Code Microservice")

# dev mode
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],  
    allow_headers=["*"],
)

# @app.post("/generate_qr_code/", response_class=FileResponse)
# async def create_qr_code(config: QRCodeConfig):
#     try:
#         path = generate_qr_code(config.data, config.filename)
#         return FileResponse(path, media_type='image/png', filename=config.filename)
#     except ValidationError as e:
#         raise HTTPException(status_code=400, detail=e.errors())
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {e}")

@app.post("/generate_qr_code", response_class=StreamingResponse)
async def create_qr_code(req: QRRequest):
    try:
        img_stream = generate_qr_code(
            data=f"{req.base_url}/api/auth/qr-login?userId={req.user_id}",
            filename=req.filename
        )
        return StreamingResponse(img_stream, media_type="image/png")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get('/health')
async def health_check():
    return {"status": "ok"}

if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=9098, reload=True)
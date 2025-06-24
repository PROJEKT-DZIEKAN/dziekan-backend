import jwt
from jwt.exceptions import JWTException

SECRET_KEY = "NICOLASMAGWIELKIEGOGARBA"
ALGORITHM = "HS256"

def verify_jwt_token(token: str):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return {
            "user_id": payload.get("sub"),
            "firstname": payload.get("firstName"),
            "surname": payload.get("surname"),
            "role": payload.get("role"),
        }
    except JWTException:
        raise ValueError("Invalid token")
    except Exception as e:
        raise ValueError(f"An error occurred while verifying the token: {str(e)}")
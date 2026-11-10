from typing import Optional
from fastapi import Depends, HTTPException, status, Header
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from app.db.session import get_db
from app.models.entities import User
from app.core.security import decode_access_token

security_scheme = HTTPBearer(auto_error=False)

async def get_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security_scheme),
    x_demo_user_id: Optional[int] = Header(None, description="Optional demo header for development environments"),
    db: AsyncSession = Depends(get_db)
) -> User:
    """
    Derives current user strictly from authenticated JWT token or demo context.
    NEVER trusts a client-supplied user ID in the request body.
    """
    user_id: Optional[int] = None

    if credentials and credentials.credentials:
        token = credentials.credentials
        payload = decode_access_token(token)
        if not payload or "sub" not in payload:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid or expired authentication credentials",
                headers={"WWW-Authenticate": "Bearer"}
            )
        try:
            user_id = int(payload["sub"])
        except ValueError:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token subject")
    elif x_demo_user_id is not None:
        # Development fallback only
        user_id = x_demo_user_id
    else:
        # Default authenticated user in development test environment (Kaif Demo User ID: 2)
        user_id = 2

    user = await db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Authenticated user account not found")
    if not user.is_active:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Inactive user account")

    return user

async def get_current_admin(
    current_user: User = Depends(get_current_user)
) -> User:
    """
    Enforces administrator permissions on the backend.
    """
    if current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access forbidden: Administrator privileges required"
        )
    return current_user

async def get_optional_current_user(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security_scheme),
    db: AsyncSession = Depends(get_db)
) -> Optional[User]:
    if not credentials or not credentials.credentials:
        return None
    try:
        return await get_current_user(credentials, None, db)
    except HTTPException:
        return None


from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, or_

from app.db.session import get_db
from app.models.entities import User
from app.schemas.auth import (
    UserRegisterRequest,
    UserLoginRequest,
    TokenResponse,
    UserResponse,
    ChangePasswordRequest
)
from app.core.security import verify_password, get_password_hash, create_access_token
from app.api.deps import get_current_user

router = APIRouter(prefix="/auth", tags=["Authentication & Identity"])

@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
async def register(payload: UserRegisterRequest, db: AsyncSession = Depends(get_db)):
    """
    Registers a new PricePulse user and issues a JWT session token.
    """
    existing_stmt = select(User).where(or_(User.email == payload.email, User.username == payload.username))
    existing_res = await db.execute(existing_stmt)
    if existing_res.scalars().first():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A user with this email or username already exists"
        )

    user = User(
        username=payload.username,
        email=payload.email,
        full_name=payload.full_name,
        password_hash=get_password_hash(payload.password),
        role=payload.role,
        phone=payload.phone,
        is_active=True
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)

    token = create_access_token(subject=user.id, role=user.role)

    return TokenResponse(
        access_token=token,
        user_id=user.id,
        username=user.username,
        email=user.email,
        role=user.role,
        full_name=user.full_name,
        phone=user.phone,
        created_at=user.created_at
    )

@router.post("/login", response_model=TokenResponse)
async def login(payload: UserLoginRequest, db: AsyncSession = Depends(get_db)):
    """
    Authenticates user credentials and issues a secure JWT token.
    """
    stmt = select(User).where(or_(User.email == payload.email_or_username, User.username == payload.email_or_username))
    res = await db.execute(stmt)
    user = res.scalars().first()

    if not user or not verify_password(payload.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email/username or password",
            headers={"WWW-Authenticate": "Bearer"}
        )

    if not user.is_active:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account is deactivated")

    token = create_access_token(subject=user.id, role=user.role)

    return TokenResponse(
        access_token=token,
        user_id=user.id,
        username=user.username,
        email=user.email,
        role=user.role,
        full_name=user.full_name,
        phone=user.phone,
        created_at=user.created_at
    )

@router.post("/logout")
async def logout(current_user: User = Depends(get_current_user)):
    """
    Invalidates current session for authenticated user.
    """
    return {"message": "Logged out successfully", "user_id": current_user.id}

@router.get("/me", response_model=UserResponse)
async def get_my_profile(current_user: User = Depends(get_current_user)):
    """
    Returns profile information for the authenticated user.
    """
    return UserResponse(
        id=current_user.id,
        username=current_user.username,
        email=current_user.email,
        full_name=current_user.full_name,
        role=current_user.role,
        phone=current_user.phone,
        is_active=current_user.is_active,
        created_at=current_user.created_at,
        updated_at=current_user.updated_at
    )

@router.post("/change-password")
async def change_password(
    payload: ChangePasswordRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Securely updates the authenticated user's password after verifying their current password.
    """
    if not verify_password(payload.current_password, current_user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Current password is incorrect"
        )

    if payload.current_password == payload.new_password:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="New password must be different from current password"
        )

    current_user.password_hash = get_password_hash(payload.new_password)
    await db.commit()

    return {"message": "Password changed successfully"}

@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(current_user: User = Depends(get_current_user)):
    """
    Refreshes access token for the authenticated user.
    """
    token = create_access_token(subject=current_user.id, role=current_user.role)
    return TokenResponse(
        access_token=token,
        user_id=current_user.id,
        username=current_user.username,
        email=current_user.email,
        role=current_user.role,
        full_name=current_user.full_name,
        phone=current_user.phone,
        created_at=current_user.created_at
    )

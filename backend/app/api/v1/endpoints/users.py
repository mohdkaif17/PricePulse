from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.db.session import get_db
from app.models.entities import User
from app.schemas.auth import UserResponse, UpdateProfileRequest
from app.api.deps import get_current_user

router = APIRouter(prefix="/users", tags=["User Profile Management"])

@router.get("/profile", response_model=UserResponse)
async def get_user_profile(current_user: User = Depends(get_current_user)):
    """
    Returns the profile of the currently authenticated user.
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

@router.patch("/profile", response_model=UserResponse)
async def update_user_profile(
    payload: UpdateProfileRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Updates the authenticated user's profile (full_name and/or phone).
    """
    if payload.full_name is not None:
        name_clean = payload.full_name.strip()
        if len(name_clean) < 2:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Full name must be at least 2 characters")
        current_user.full_name = name_clean

    if payload.phone is not None:
        current_user.phone = payload.phone.strip()

    await db.commit()
    await db.refresh(current_user)

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


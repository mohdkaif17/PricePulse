import pytest
import asyncio
from httpx import AsyncClient, ASGITransport
import sys
import os

# Add backend to path
sys.path.append(os.path.join(os.path.dirname(__file__), ".."))

from app.main import app

@pytest.mark.asyncio
async def test_auth_and_profile_complete_lifecycle():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # TEST 1: Register unique user
        unique_suffix = os.urandom(4).hex()
        email = f"user_{unique_suffix}@pricepulse.in"
        username = f"usr_{unique_suffix}"
        reg_payload = {
            "username": username,
            "email": email,
            "password": "StrongPassword123",
            "full_name": "Arjun Sharma",
            "phone": "+919876543210",
            "role": "user"
        }
        res = await ac.post("/api/v1/auth/register", json=reg_payload)
        assert res.status_code == 201, f"Register failed: {res.text}"
        data = res.json()
        assert "access_token" in data
        assert data["email"] == email
        assert data["full_name"] == "Arjun Sharma"
        assert data["phone"] == "+919876543210"

        # TEST 2: Duplicate registration rejected
        res_dup = await ac.post("/api/v1/auth/register", json=reg_payload)
        assert res_dup.status_code == 400
        assert "already exists" in res_dup.json()["detail"]

        # TEST 3: Login with invalid password fails
        login_fail = await ac.post("/api/v1/auth/login", json={
            "email_or_username": email,
            "password": "WrongPassword!"
        })
        assert login_fail.status_code == 401

        # TEST 4: Login with valid credentials succeeds
        login_ok = await ac.post("/api/v1/auth/login", json={
            "email_or_username": email,
            "password": "StrongPassword123"
        })
        assert login_ok.status_code == 200
        token = login_ok.json()["access_token"]
        assert token
        headers = {"Authorization": f"Bearer {token}"}

        # TEST 5: GET /api/v1/auth/me returns authentic user data
        me_res = await ac.get("/api/v1/auth/me", headers=headers)
        assert me_res.status_code == 200
        me = me_res.json()
        assert me["email"] == email
        assert me["full_name"] == "Arjun Sharma"
        assert me["phone"] == "+919876543210"
        assert me["is_active"] is True
        assert "password_hash" not in me

        # TEST 6: GET /api/v1/users/profile
        prof_res = await ac.get("/api/v1/users/profile", headers=headers)
        assert prof_res.status_code == 200
        assert prof_res.json()["email"] == email

        # TEST 7: PATCH /api/v1/users/profile updates details
        patch_res = await ac.patch("/api/v1/users/profile", json={
            "full_name": "Arjun S. Bengaluru",
            "phone": "+919999888877"
        }, headers=headers)
        assert patch_res.status_code == 200
        updated = patch_res.json()
        assert updated["full_name"] == "Arjun S. Bengaluru"
        assert updated["phone"] == "+919999888877"

        # TEST 8: Change password with incorrect current password fails
        pw_fail = await ac.post("/api/v1/auth/change-password", json={
            "current_password": "WrongCurrentPassword",
            "new_password": "BrandNewPassword456"
        }, headers=headers)
        assert pw_fail.status_code == 400

        # TEST 9: Change password succeeds
        pw_ok = await ac.post("/api/v1/auth/change-password", json={
            "current_password": "StrongPassword123",
            "new_password": "BrandNewPassword456"
        }, headers=headers)
        assert pw_ok.status_code == 200

        # TEST 10: Old password fails to login
        old_login = await ac.post("/api/v1/auth/login", json={
            "email_or_username": email,
            "password": "StrongPassword123"
        })
        assert old_login.status_code == 401

        # TEST 11: New password logs in successfully
        new_login = await ac.post("/api/v1/auth/login", json={
            "email_or_username": email,
            "password": "BrandNewPassword456"
        })
        assert new_login.status_code == 200
        new_token = new_login.json()["access_token"]
        assert new_token

        # TEST 12: Logout endpoint
        logout_res = await ac.post("/api/v1/auth/logout", headers={"Authorization": f"Bearer {new_token}"})
        assert logout_res.status_code == 200

    print("ALL 12 AUTHENTICATION & PROFILE INTEGRATION TESTS PASSED!")

if __name__ == "__main__":
    asyncio.run(test_auth_and_profile_complete_lifecycle())


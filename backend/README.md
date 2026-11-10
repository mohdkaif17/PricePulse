# PricePulse Backend API

FastAPI-based price comparison and location-aware intelligence service for the PricePulse Android application.

## System Requirements
- Python 3.12+ (64-bit)
- PostgreSQL 15+ with PostGIS extension (e.g. PostgreSQL 18.6)

## Directory Structure
```
backend/
├── app/
│   ├── api/
│   │   └── v1/
│   │       ├── endpoints/
│   │       │   ├── health.py
│   │       │   ├── auth.py
│   │       │   ├── categories.py
│   │       │   ├── products.py
│   │       │   ├── shops.py
│   │       │   ├── compare.py
│   │       │   └── history.py
│   │       └── router.py
│   ├── core/
│   │   ├── config.py
│   │   ├── logging.py
│   │   ├── errors.py
│   │   └── security.py
│   ├── db/
│   │   ├── session.py
│   │   └── base.py
│   ├── models/
│   ├── schemas/
│   ├── services/
│   └── main.py
├── tests/
├── .env
├── .env.example
├── requirements.txt
└── README.md
```

## Setup Instructions

1. **Install Dependencies:**
   ```powershell
   py -3.12 -m pip install -r requirements.txt
   ```

2. **Configure Environment:**
   Copy `.env.example` to `.env` and configure your credentials:
   ```powershell
   cp .env.example .env
   ```

3. **Run Backend Server:**
   ```powershell
   cd backend
   py -3.12 -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```

4. **Verify Health Endpoint:**
   - Interactive Swagger Docs: `http://localhost:8000/api/v1/docs`
   - Health check: `http://localhost:8000/api/v1/health`
   - Android Emulator URL: `http://10.0.2.2:8000/api/v1/health`

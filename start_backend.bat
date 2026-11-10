@echo off
title PricePulse Backend Server
echo Starting PricePulse FastAPI Backend on http://0.0.0.0:8000 ...

set PYTHON_EXE=C:\Users\kaifi\AppData\Local\Programs\Python\Python312\python.exe
if not exist "%PYTHON_EXE%" (
    set PYTHON_EXE=python
)

cd /d "%~dp0backend"
"%PYTHON_EXE%" -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause


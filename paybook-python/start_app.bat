@echo off
setlocal

cd /d "%~dp0"

where py >nul 2>&1
if errorlevel 1 (
    echo Python launcher was not found. Install Python 3 and try again.
    pause
    exit /b 1
)

where npm >nul 2>&1
if errorlevel 1 (
    echo npm was not found. Install Node.js and try again.
    pause
    exit /b 1
)

if exist "backend\.venv\Scripts\python.exe" goto backend_environment_ready

echo Creating backend Python environment...
py -3 -m venv "backend\.venv"
if errorlevel 1 (
    echo Could not create the backend Python environment.
    pause
    exit /b 1
)

echo Installing backend dependencies...
call "backend\.venv\Scripts\python.exe" -m pip install -r "backend\requirements.txt"
if errorlevel 1 (
    echo Backend dependency installation failed.
    pause
    exit /b 1
)

:backend_environment_ready

"backend\.venv\Scripts\python.exe" -c "import fastapi, uvicorn, sqlalchemy, pymysql, pydantic, pydantic_settings, dotenv, cryptography" >nul 2>&1
if not errorlevel 1 goto backend_dependencies_ready

echo Installing backend dependencies...
call "backend\.venv\Scripts\python.exe" -m pip install -r "backend\requirements.txt"
if errorlevel 1 (
    echo Backend dependency installation failed.
    pause
    exit /b 1
)

:backend_dependencies_ready

if not exist "frontend\node_modules\vite" (
    echo Installing frontend dependencies...
    call npm --prefix "frontend" install
    if errorlevel 1 (
        echo Frontend dependency installation failed.
        pause
        exit /b 1
    )
)

if exist "backend\.env" goto start_backend_with_env
start "PayBook Backend" /D "%~dp0backend" cmd /k ".venv\Scripts\python.exe -m uvicorn app.main:app --reload"
goto start_frontend

:start_backend_with_env
start "PayBook Backend" /D "%~dp0backend" cmd /k ".venv\Scripts\python.exe -m uvicorn app.main:app --reload --env-file .env"

:start_frontend
start "PayBook Frontend" /D "%~dp0frontend" cmd /k "npm run dev"

echo.
echo Starting locally without Docker.
echo Frontend:             http://localhost:8080/Paybook/
echo API documentation:    http://localhost:8000/docs
start "" "http://localhost:8080/Paybook/"
exit /b 0
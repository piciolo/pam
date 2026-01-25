@echo off
REM ============================================
REM PAM PDF to Excel - Download Libraries
REM ============================================

echo.
echo [INFO] Download delle librerie JAR...
echo.

cd /d "%~dp0"

REM Usa Python per scaricare le librerie
if exist "f:\PAM\.venv\Scripts\python.exe" (
    f:\PAM\.venv\Scripts\python.exe download_libraries.py
) else (
    echo [ERR] Python non trovato in f:\PAM\.venv
    echo.
    echo Scarica manualmente le librerie da:
    echo https://repo1.maven.org/maven2/
    echo.
    echo Crea la cartella lib\ e copia i JAR necessari (POI, Commons)
    pause
    exit /b 1
)

echo.
pause

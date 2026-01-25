@echo off
REM ============================================
REM PAM PDF to Excel - Compile Script
REM ============================================

setlocal enabledelayedexpansion

echo.
echo [INFO] Compilazione del progetto Java...
echo.

cd /d "%~dp0"

REM Crea le directory necessarie
if not exist build mkdir build
if not exist dist mkdir dist

REM Compila il codice
echo [STEP 1] Compilazione del codice sorgente...
javac -cp "lib\*" -d build src\*.java

if errorlevel 1 (
    echo [ERR] Errore durante la compilazione
    pause
    exit /b 1
)

echo [STEP 2] Creazione del JAR...
f:\PAM\.venv\Scripts\python.exe create_jar.py

if errorlevel 1 (
    echo [ERR] Errore durante la creazione del JAR
    pause
    exit /b 1
)

echo.
echo [OK] Compilazione completata!
echo.
echo Per avviare l'applicazione, esegui:
echo   run.bat
echo.
pause

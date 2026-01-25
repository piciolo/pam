@echo off
REM ============================================
REM PAM PDF to Excel - Launcher (SEMPLIFICATO)
REM ============================================

setlocal enabledelayedexpansion

cls
echo.
echo ============================================
echo  PAM PDF to Excel v1.0
echo  Estrazione Automatica PDF a Foglio Excel
echo ============================================
echo.

REM Vai nella directory del progetto
cd /d "%~dp0"

echo [INFO] Cartella: %cd%
echo [INFO] Estraendo classi...

REM Estrai le classi
f:\PAM\.venv\Scripts\python.exe extract_classes.py

echo.
echo [INFO] Avvio applicazione...
echo.

REM Lancia l'app
java -cp "classes;lib\*" MainWindow

echo.
echo [INFO] Applicazione chiusa
echo.
pause

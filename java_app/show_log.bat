@echo off
REM ============================================
REM Mostra il file di log dell'esecuzione
REM ============================================

setlocal enabledelayedexpansion

cd /d "%~dp0"

if not exist "run_debug.log" (
    echo [ERR] File log non trovato: run_debug.log
    echo.
    echo Esegui run.bat prima di usare questo script
    pause
    exit /b 1
)

cls
echo.
echo ============================================
echo  PAM PDF to Excel - Debug Log
echo ============================================
echo.
echo Contenuto di: run_debug.log
echo ============================================
echo.

type run_debug.log

echo.
echo ============================================
echo Fine log
echo ============================================
echo.
pause

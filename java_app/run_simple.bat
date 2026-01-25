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

REM Controlla che log4j sia presente (necessario per Apache POI 5.x)
if not exist "lib\\log4j-api-2.22.1.jar" (
    echo [ERR] log4j-api-2.22.1.jar non trovato - Esegui download_libraries.bat
    echo.
    pause
    exit /b 1
)
if not exist "lib\\log4j-core-2.22.1.jar" (
    echo [ERR] log4j-core-2.22.1.jar non trovato - Esegui download_libraries.bat
    echo.
    pause
    exit /b 1
)

REM Controlla commons-io minimo (necessario per Apache POI 5.2.5)
if not exist "lib\\commons-io-2.13.0.jar" (
    echo [ERR] commons-io-2.13.0.jar non trovato - Esegui download_libraries.bat
    echo.
    pause
    exit /b 1
)
if exist "lib\\commons-io-2.11.0.jar" (
    echo [ERR] Trovato commons-io-2.11.0.jar (obsoleto). Eliminalo o riesegui download_libraries.bat
    echo.
    pause
    exit /b 1
)

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

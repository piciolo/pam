@echo off
REM ============================================
REM Test Environment Check
REM ============================================

setlocal enabledelayedexpansion

cls
echo.
echo ============================================
echo  PAM PDF to Excel - Environment Check
echo ============================================
echo.

REM Controlla Java
echo [TEST 1] Verifica Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Java NON trovato - Installa Java
    goto error
) else (
    echo [OK] Java trovato
)

REM Controlla Python
echo.
echo [TEST 2] Verifica Python...
if exist "f:\PAM\.venv\Scripts\python.exe" (
    f:\PAM\.venv\Scripts\python.exe --version >nul 2>&1
    if errorlevel 1 (
        echo [FAIL] Python Virtual Env non funziona
        goto error
    ) else (
        echo [OK] Python trovato in .venv
    )
) else (
    echo [FAIL] Python Virtual Env non trovato in f:\PAM\.venv
    goto error
)

REM Controlla pdfplumber
echo.
echo [TEST 3] Verifica pdfplumber...
f:\PAM\.venv\Scripts\python.exe -c "import pdfplumber; print('OK')" >nul 2>&1
if errorlevel 1 (
    echo [FAIL] pdfplumber NON disponibile
    goto error
) else (
    echo [OK] pdfplumber disponibile
)

REM Controlla cartelle
echo.
echo [TEST 4] Verifica cartelle...
if not exist "src" (
    echo [FAIL] Cartella src\ non trovata
    goto error
) else (
    echo [OK] Cartella src\ trovata
)

REM Controlla file sorgente
echo.
echo [TEST 5] Verifica file sorgente...
if not exist "src\MainWindow.java" (
    echo [FAIL] MainWindow.java non trovato
    goto error
) else (
    echo [OK] MainWindow.java trovato
)

if not exist "src\PDFDataExtractor.java" (
    echo [FAIL] PDFDataExtractor.java non trovato
    goto error
) else (
    echo [OK] PDFDataExtractor.java trovato
)

if not exist "src\ExcelHandler.java" (
    echo [FAIL] ExcelHandler.java non trovato
    goto error
) else (
    echo [OK] ExcelHandler.java trovato
)

REM Tutti i test passati
echo.
echo ============================================
echo [SUCCESS] Tutti i test passati!
echo ============================================
echo.
echo Prossimi step:
echo 1. Esegui: download_libraries.bat
echo 2. Esegui: compile.bat
echo 3. Esegui: run.bat
echo.
pause
exit /b 0

:error
echo.
echo ============================================
echo [FAILED] Ambiente non correttamente configurato
echo ============================================
echo.
echo Contatta il team IT per:
echo - Installare Java JRE 11+
echo - Configurare Python correttamente
echo.
pause
exit /b 1

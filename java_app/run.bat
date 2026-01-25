@echo off
REM ============================================
REM PAM PDF to Excel - Launcher with Logging
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
echo [LOG] Cartella attuale: %cd% >> run_debug.log
echo [LOG] Data/Ora: %date% %time% >> run_debug.log

REM Controlla se il JAR esiste
echo [LOG] Verificando JAR file... >> run_debug.log
if not exist "dist\PAM-PDF-to-Excel.jar" (
    echo [ERR] File JAR non trovato: dist\PAM-PDF-to-Excel.jar
    echo [ERR] JAR non trovato >> run_debug.log
    echo.
    echo Per compilare il progetto, esegui:
    echo   1. download_libraries.bat (scarica le librerie)
    echo   2. compile.bat (compila il codice)
    echo.
    pause
    exit /b 1
) else (
    echo [OK] JAR trovato >> run_debug.log
    for %%A in (dist\PAM-PDF-to-Excel.jar) do (
        echo [LOG] Dimensione JAR: %%~zA bytes >> run_debug.log
    )
)

REM Controlla se le librerie esistono
echo [LOG] Verificando cartella lib... >> run_debug.log
if not exist "lib" (
    echo [ERR] Cartella lib\ non trovata
    echo [ERR] Cartella lib non trovata >> run_debug.log
    echo.
    echo Per compilare il progetto, esegui:
    echo   1. download_libraries.bat (scarica le librerie)
    echo   2. compile.bat (compila il codice)
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Cartella lib trovata >> run_debug.log
)

REM Controlla che log4j sia presente (necessario per Apache POI 5.x)
if not exist "lib\\log4j-api-2.22.1.jar" (
    echo [ERR] log4j-api-2.22.1.jar mancante >> run_debug.log
    echo [ERR] log4j-api-2.22.1.jar non trovato - Esegui download_libraries.bat
    echo.
    echo Per aggiornare le librerie, esegui:
    echo   download_libraries.bat
    echo.
    pause
    exit /b 1
)
if not exist "lib\\log4j-core-2.22.1.jar" (
    echo [ERR] log4j-core-2.22.1.jar mancante >> run_debug.log
    echo [ERR] log4j-core-2.22.1.jar non trovato - Esegui download_libraries.bat
    echo.
    echo Per aggiornare le librerie, esegui:
    echo   download_libraries.bat
    echo.
    pause
    exit /b 1
)

REM Controlla commons-io minimo (necessario per Apache POI 5.2.5)
if not exist "lib\\commons-io-2.13.0.jar" (
    echo [ERR] commons-io-2.13.0.jar non trovato - Esegui download_libraries.bat >> run_debug.log
    echo [ERR] commons-io-2.13.0.jar non trovato - Esegui download_libraries.bat
    echo.
    echo Per aggiornare le librerie, esegui:
    echo   download_libraries.bat
    echo.
    pause
    exit /b 1
)
if exist "lib\\commons-io-2.11.0.jar" (
    echo [ERR] commons-io-2.11.0.jar obsoleto trovato - Rimuovilo o esegui download_libraries.bat >> run_debug.log
    echo [ERR] commons-io-2.11.0.jar obsoleto trovato - Rimuovilo o esegui download_libraries.bat
    echo.
    pause
    exit /b 1
)

REM Controlla Java
echo [LOG] Verificando Java... >> run_debug.log
java -version >> run_debug.log 2>&1
if errorlevel 1 (
    echo [ERR] Java non trovato >> run_debug.log
    echo [ERR] Java NON trovato - Installa Java
    pause
    exit /b 1
) else (
    echo [OK] Java trovato >> run_debug.log
)

REM Estrai le classi dal JAR se non sono già estratte
echo [LOG] Verificando cartella classes... >> run_debug.log
if not exist "classes" (
    echo [LOG] Estraendo classi da JAR... >> run_debug.log
    f:\PAM\.venv\Scripts\python.exe extract_classes.py >> run_debug.log 2>&1
) else (
    echo [LOG] Cartella classes già esiste >> run_debug.log
)

REM Avvia l'applicazione
echo [LOG] Lanciando applicazione... >> run_debug.log
echo [LOG] Comando: java -cp "%MYDIR%\classes;%MYDIR%\lib\*" MainWindow >> run_debug.log
echo.
echo [INFO] Avvio applicazione...
echo [INFO] Un log dettagliato e' stato salvato in: run_debug.log
echo.

REM Lancia con output redirection
start "PAM PDF to Excel" java -cp "%MYDIR%\classes;%MYDIR%\lib\*" MainWindow 1>> run_debug.log 2>&1

echo [LOG] Applicazione avviata con codice: %ERRORLEVEL% >> run_debug.log
echo [INFO] Applicazione avviata. Chiudi questa finestra quando hai finito.
echo.
pause

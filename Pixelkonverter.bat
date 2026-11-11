@echo off
rem Startet den Pixelkonverter (Foto -> C64/SKALD-Pixelgrafik) unter Windows.
rem Ein Bild kann auch direkt auf diese Datei gezogen werden.
setlocal
cd /d "%~dp0"

set PY=py -3
where py >nul 2>&1 || set PY=python
%PY% --version >nul 2>&1
if errorlevel 1 (
    echo Python wurde nicht gefunden.
    echo Bitte Python 3 von https://www.python.org/downloads/ installieren
    echo und im Installer "Add python.exe to PATH" ankreuzen.
    pause
    exit /b 1
)

%PY% -c "import PIL, numpy" >nul 2>&1
if errorlevel 1 (
    echo Installiere benoetigte Pakete: pillow, numpy ...
    %PY% -m pip install --user pillow numpy
    if errorlevel 1 (
        echo Installation fehlgeschlagen.
        pause
        exit /b 1
    )
)

%PY% tools\photo2pixel_gui.py %*
if errorlevel 1 pause

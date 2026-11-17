@echo off
cd /d "%~dp0"
echo ========================================================
echo    WaferScope AOI - Semiconductor Defect Detection
echo ========================================================
if not exist out mkdir out
echo [1/2] Compiling Java Swing application...
javac -encoding UTF-8 -d out -sourcepath src src\Main.java src\waferscope\model\*.java src\waferscope\io\*.java src\waferscope\classifier\*.java src\waferscope\datastructures\*.java src\waferscope\ui\*.java src\test\*.java
if errorlevel 1 (
    echo [ERROR] Compilation failed. Please check JDK 11+ is installed.
    pause
    exit /b 1
)
echo [2/2] Launching WaferScope AOI Desktop Interface...
start "" javaw -cp out Main
echo Application launched successfully.

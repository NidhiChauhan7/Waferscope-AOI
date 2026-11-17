@echo off
cd /d "%~dp0"
echo ====================================================================
echo   Course 3: Computer Graphics Laboratory in C++ (2310218L)
echo ====================================================================

where g++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Compiling C++ Computer Graphics Engine with g++: ComputerGraphicsDemo.cpp...
    g++ -O3 -std=c++17 ComputerGraphicsDemo.cpp -o ComputerGraphicsDemo.exe
    if %errorlevel% equ 0 (
        echo [SUCCESS] C++ Computer Graphics binary compiled successfully!
        echo Running C++ Computer Graphics Demonstration...
        ComputerGraphicsDemo.exe
        echo.
        if "%1" neq "--no-pause" pause
        exit /b 0
    )
)

where clang++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Compiling C++ Computer Graphics Engine with clang++: ComputerGraphicsDemo.cpp...
    clang++ -O3 -std=c++17 ComputerGraphicsDemo.cpp -o ComputerGraphicsDemo.exe
    if %errorlevel% equ 0 (
        echo [SUCCESS] C++ Computer Graphics binary compiled successfully!
        echo Running C++ Computer Graphics Demonstration...
        ComputerGraphicsDemo.exe
        echo.
        if "%1" neq "--no-pause" pause
        exit /b 0
    )
)

echo [INFO] No C++ compiler - g++ or clang++ - found in system PATH.
echo Java Swing connects directly to the C++ Algorithm Bridge automatically.
if exist ComputerGraphicsDemo.java (
    javac ComputerGraphicsDemo.java WaferCircleAlgorithms.java WaferSurfaceShading.java WaferTransformations.java
    java ComputerGraphicsDemo
)
echo.
if "%1" neq "--no-pause" pause

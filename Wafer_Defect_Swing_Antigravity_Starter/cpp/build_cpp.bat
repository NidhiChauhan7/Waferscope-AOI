@echo off
cd /d "%~dp0"
echo ========================================================
echo   Compiling C++ Graphics, Preprocessor, and Data Structures
echo ========================================================
where g++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Compiling wafer_graphics.cpp with g++...
    g++ -O3 -std=c++17 wafer_graphics.cpp -o wafer_graphics.exe
    echo Compiling wafer_preprocessor.cpp with g++...
    g++ -O3 -std=c++17 wafer_preprocessor.cpp -o wafer_preprocessor.exe
    echo Compiling wafer_datastructures.cpp with g++...
    g++ -O3 -std=c++17 wafer_datastructures.cpp -o wafer_datastructures.exe
    echo [SUCCESS] C++ binaries compiled successfully!
    exit /b 0
)

where clang++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Compiling with clang++...
    clang++ -O3 -std=c++17 wafer_graphics.cpp -o wafer_graphics.exe
    clang++ -O3 -std=c++17 wafer_preprocessor.cpp -o wafer_preprocessor.exe
    clang++ -O3 -std=c++17 wafer_datastructures.cpp -o wafer_datastructures.exe
    echo [SUCCESS] C++ binaries compiled successfully!
    exit /b 0
)

echo [INFO] No C++ compiler (g++ / clang++) found in system PATH.
echo Java Swing will use the C++ Algorithm Bridge automatically.
exit /b 0

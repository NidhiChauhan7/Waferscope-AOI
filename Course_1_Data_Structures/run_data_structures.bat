@echo off
cd /d "%~dp0"
echo ====================================================================
echo    Data Structures Laboratory in C++ - Semiconductor Wafer Inspection
echo ====================================================================

where g++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Compiling C++ Data Structures with g++: DataStructuresDemo.cpp...
    g++ -O3 -std=c++17 DataStructuresDemo.cpp -o DataStructuresDemo.exe
    if %errorlevel% equ 0 (
        echo [SUCCESS] C++ Data Structures binary compiled!
        echo Running C++ Data Structures Demonstration...
        DataStructuresDemo.exe
        echo.
        pause
        exit /b 0
    )
)

where clang++ >nul 2>nul
if %errorlevel% equ 0 (
    echo Compiling C++ Data Structures with clang++: DataStructuresDemo.cpp...
    clang++ -O3 -std=c++17 DataStructuresDemo.cpp -o DataStructuresDemo.exe
    if %errorlevel% equ 0 (
        echo [SUCCESS] C++ Data Structures binary compiled!
        echo Running C++ Data Structures Demonstration...
        DataStructuresDemo.exe
        echo.
        pause
        exit /b 0
    )
)

echo [INFO] No C++ compiler - g++ or clang++ - found in system PATH.
echo Running synchronized Java and C++ Algorithm Demonstration...
javac -encoding UTF-8 *.java
java -cp . DataStructuresDemo
echo.
pause

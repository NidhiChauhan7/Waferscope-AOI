@echo off
cd /d "%~dp0"
echo ========================================================
echo    Course 4: PSOOP Lab (C++ - 2310261L) - Demonstration
echo ========================================================
where g++ >nul 2>nul
if errorlevel 1 (
    echo Note: g++ compiler not found in system PATH.
    echo To compile and run manually in Code::Blocks or VS:
    echo g++ -std=c++17 PsoopCppDemo.cpp -o PsoopCppDemo.exe
    echo PsoopCppDemo.exe
    pause
    exit /b 0
)
echo Compiling C++ OOP code with g++...
g++ -std=c++17 PsoopCppDemo.cpp -o PsoopCppDemo.exe
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
echo Running C++ OOP Demonstration...
PsoopCppDemo.exe
echo.
pause

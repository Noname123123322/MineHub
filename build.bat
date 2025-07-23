@echo off
REM MineHub Velocity Plugin Build Script for Windows
REM This script builds the plugin and creates a distribution package

echo Building MineHub Velocity Plugin...

REM Clean previous builds
echo Cleaning previous builds...
mvn clean

REM Compile and package
echo Compiling and packaging...
mvn package -DskipTests

REM Check if build was successful
if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo Plugin JAR created at: target/minehub-velocity-plugin-1.0.0.jar

    REM Create distribution directory
    if not exist dist mkdir dist

    REM Copy JAR to dist
    copy target\minehub-velocity-plugin-1.0.0.jar dist\

    REM Copy documentation
    copy README.md dist\
    copy LICENSE dist\
    copy CHANGELOG.md dist\
    xcopy docs dist\docs\ /E /I

    echo Distribution package created in 'dist' directory
) else (
    echo Build failed! Check the error messages above.
    exit /b 1
)

pause
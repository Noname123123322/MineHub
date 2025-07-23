#!/bin/bash

# MineHub Velocity Plugin Build Script
# This script builds the plugin and creates a distribution package

echo "Building MineHub Velocity Plugin..."

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Compile and package
echo "Compiling and packaging..."
mvn package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Plugin JAR created at: target/minehub-velocity-plugin-1.0.0.jar"

    # Create distribution directory
    mkdir -p dist

    # Copy JAR to dist
    cp target/minehub-velocity-plugin-1.0.0.jar dist/

    # Copy documentation
    cp README.md dist/
    cp LICENSE dist/
    cp CHANGELOG.md dist/
    cp -r docs dist/

    echo "Distribution package created in 'dist' directory"
else
    echo "Build failed! Check the error messages above."
    exit 1
fi
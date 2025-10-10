#!/bin/bash

# Simple build script for CCAI Java project
# This script demonstrates how to compile the project manually

echo "Building CCAI Java project..."

# Create output directory
mkdir -p target/classes

# Compile the Java sources
echo "Compiling Java sources..."
find src/main/java -name "*.java" > sources.txt

if [ -s sources.txt ]; then
    javac -cp ".:src/main/resources" -d target/classes @sources.txt
    if [ $? -eq 0 ]; then
        echo "✅ Compilation successful!"
    else
        echo "❌ Compilation failed!"
        exit 1
    fi
else
    echo "No Java source files found"
    exit 1
fi

# Copy resources
echo "Copying resources..."
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* target/classes/ 2>/dev/null || true
fi

# Create JAR file
echo "Creating JAR file..."
cd target/classes
jar cf ../ccai-java-1.0.0.jar .
cd ../..

echo "✅ Build completed successfully!"
echo "JAR file created: target/ccai-java-1.0.0.jar"

# Clean up
rm -f sources.txt

echo ""
echo "To run the examples, you would typically use:"
echo "java -cp target/ccai-java-1.0.0.jar:dependencies/* com.cloudcontactai.ccai.examples.BasicSMSExample"
echo ""
echo "Note: This project requires Spring Boot dependencies which are not included in this simple build."
echo "For a complete build, please use Maven: mvn clean package"

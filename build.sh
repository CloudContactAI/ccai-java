#!/bin/bash

# CCAI Java SDK Build Script

set -e

echo "Building CCAI Java SDK..."

# Clean previous builds
echo "Cleaning previous builds..."
./mvnw clean

# Compile and run tests
echo "Compiling and running tests..."
./mvnw compile test

# Package the library
echo "Packaging library..."
./mvnw package

# Install to local repository
echo "Installing to local Maven repository..."
./mvnw install

echo "Build completed successfully!"
echo "JAR file location: target/ccai-java-sdk-1.0.0.jar"
echo "Sources JAR: target/ccai-java-sdk-1.0.0-sources.jar"
echo "Javadoc JAR: target/ccai-java-sdk-1.0.0-javadoc.jar"

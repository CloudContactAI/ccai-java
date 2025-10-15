#!/bin/bash

set -e

echo "Building Maven project..."
./mvnw clean package -DskipTests

echo "Creating deployment structure..."
rm -rf central-bundle
mkdir -p central-bundle/com/cloudcontactai/ccai-java-sdk/1.0.0

# Copy artifacts
cp target/ccai-java-sdk-1.0.0.jar central-bundle/com/cloudcontactai/ccai-java-sdk/1.0.0/
cp target/ccai-java-sdk-1.0.0-sources.jar central-bundle/com/cloudcontactai/ccai-java-sdk/1.0.0/
cp pom.xml central-bundle/com/cloudcontactai/ccai-java-sdk/1.0.0/ccai-java-sdk-1.0.0.pom

# Create javadoc jar
mkdir -p temp-javadoc
echo "# CCAI Java SDK Documentation" > temp-javadoc/README.md
echo "Visit https://github.com/cloudcontactai/ccai-java-sdk for documentation" >> temp-javadoc/README.md
cd temp-javadoc
jar cf ../central-bundle/com/cloudcontactai/ccai-java-sdk/1.0.0/ccai-java-sdk-1.0.0-javadoc.jar *
cd ..
rm -rf temp-javadoc

cd central-bundle/com/cloudcontactai/ccai-java-sdk/1.0.0

echo "Generating checksums and signatures..."
for file in *.jar *.pom; do
    # Generate checksums
    md5sum "$file" | cut -d' ' -f1 > "$file.md5"
    sha1sum "$file" | cut -d' ' -f1 > "$file.sha1"
    
    # Sign with GPG
    gpg --armor --detach-sign "$file"
    
    echo "Processed $file"
done

cd ../../../../..

echo "Creating ZIP archive..."
cd central-bundle
zip -r ../ccai-java-sdk-1.0.0-central-bundle.zip com/
cd ..

echo "Bundle created: ccai-java-sdk-1.0.0-central-bundle.zip"
echo "Upload this file to Maven Central Publisher Portal"

# Show structure
echo -e "\nBundle structure:"
unzip -l ccai-java-sdk-1.0.0-central-bundle.zip

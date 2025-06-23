#!/bin/bash
echo "Building NetTalk application..."

# Clean and package with Maven
mvn clean package

echo ""
echo "Build completed!"
echo ""
echo "JAR file is located at: target/NetTalk.jar"
echo ""
echo "Run with: java -jar target/NetTalk.jar"
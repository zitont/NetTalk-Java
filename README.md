# NetTalk Chat Application

A Java-based chat application with server and client functionality.

## Building the Application

### Prerequisites
- Java JDK 11 or higher
- Maven 3.6 or higher

### Build Steps
1. Clone the repository
2. Run the build script:
   - On Windows: `build.bat`
   - On Linux/Mac: `./build.sh`

The executable will be created in the `target` directory.

## Running the Application

### Windows
Double-click the `NetTalk.exe` file in the `target` directory.

### Other Platforms
Run the JAR file:
```
java -jar target/NetTalk.jar
```

## Configuration
The application uses a configuration file located at `src/main/resources/config.properties`.
You can modify this file to change server settings.

## Features
- Chat with multiple users
- Private messaging
- Server mode for hosting your own chat server
- AI-powered translation
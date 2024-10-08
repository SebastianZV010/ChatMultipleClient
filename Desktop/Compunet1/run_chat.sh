#!/bin/bash

# Ruta del proyecto donde se encuentran los JARs
PROJECT_DIR="/Users/AiProjects/Desktop/Compunet1"

# Cambiar al directorio del proyecto
cd "$PROJECT_DIR" || { echo "Failed to navigate to project directory: $PROJECT_DIR"; exit 1; }

# Limpiar, construir y crear los JARs
echo "Cleaning, building and creating jars..."
./gradlew clean build serverJar clientJar

# Comprobar si la construcción fue exitosa
if [ $? -ne 0 ]; then
  echo "Build failed. Exiting."
  exit 1
fi

echo "Build successful!"

# Ruta de los JARs
SERVER_JAR="./build/libs/ChatServer-1.0-SNAPSHOT.jar"
CLIENT_JAR="./build/libs/ChatClient-1.0-SNAPSHOT.jar"

# Comprobación de la existencia de los JARs
if [ ! -f "$SERVER_JAR" ] || [ ! -f "$CLIENT_JAR" ]; then
  echo "Error: JARs not found."
  exit 1
fi

# Iniciar el servidor en una nueva terminal en Mac
echo "Starting the server..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_DIR"' && java -jar '"$SERVER_JAR"'"'

# Pausar unos segundos para dar tiempo al servidor de iniciar
sleep 2

# Iniciar los clientes en nuevas ventanas de terminal en Mac
echo "Starting clients..."
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_DIR"' && java -jar '"$CLIENT_JAR"'"'
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_DIR"' && java -jar '"$CLIENT_JAR"'"'
osascript -e 'tell application "Terminal" to do script "cd '"$PROJECT_DIR"' && java -jar '"$CLIENT_JAR"'"'

echo "Server and clients started successfully."

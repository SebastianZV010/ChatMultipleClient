# Sistema Multichat - Compunet1

Este es un sistema multichat en Java utilizando TCP y UDP, que permite a los usuarios enviar mensajes de texto, notas de voz, crear grupos y realizar algunas funcionalidades básicas de llamadas.

## Requisitos

Para ejecutar este proyecto, necesitas:

- **Java 8** o superior
- **Gradle** 
- **Micrófono** y **altavoces** (para grabar y reproducir notas de voz)

## Instrucciones para la ejecución

### 1. Clonar el repositorio

Primero, clona el repositorio en tu máquina local:

```bash
git clone https://github.com/SebastianZV010/ChatMultipleClient.git
cd Compunet1
```

### 2. Compilar el proyecto


El proyecto usa Gradle para la gestión de dependencias y la compilación. Para compilar el proyecto, ejecuta el siguiente comando en la raíz del proyecto:

```bash
./gradlew build
cd build/libs
```

Este comando generará los archivos `.jar` del cliente y servidor en la carpeta `libs`.

Otra manera es poner el comando `./run_chat.sh` el cual es un scrip que genra los jar, los ejecuta y te abre las termianles. Este abre 4 termianles, 1 servidor y 3 clientes.

```bash
./run_chat.sh
```

### 3. Ejecutar el servidor

Para iniciar el servidor, debes navegar a la carpeta `libs` y ejecutar el siguiente comando:

```bash
java -jar libs/server.jar
```

El servidor se ejecutará en el puerto 8080 y estará listo para aceptar conexiones de clientes.

### 4. Ejecutar el cliente

Para iniciar el cliente, abre una nueva terminal y ejecuta el siguiente comando desde la carpeta `libs`:

```bash
java -jar libs/client.jar
```

Al ejecutar el cliente, se te pedirá que ingreses un nombre de usuario. Una vez conectado al servidor, podrás empezar a enviar mensajes o notas de voz a otros usuarios.

### 5. Enviar mensajes y notas de voz

#### Enviar mensajes de texto

Para enviar un mensaje de texto a otro usuario, escribe el nombre del usuario seguido del mensaje:

```bash
user1 Hola, ¿cómo estás?
```

#### Enviar una nota de voz

Para enviar una nota de voz a un usuario o grupo, utiliza el siguiente comando:

```bash
/voice <usuario/grupo>
```

Después de ejecutar el comando, el cliente empezará a grabar la nota de voz. Para detener la grabación, presiona `p` y luego presiona Enter para enviar la nota de voz al destinatario.

### 6. Crear grupos de chat

Puedes crear un grupo de chat con varios usuarios utilizando el siguiente comando:

```bash
/group <nombre_del_grupo> <miembro1,miembro2,...>
```

Por ejemplo:

```bash
/group amigos user1,user2,user3
```

### 7. Comandos adicionales

- `/call <usuario>`: Iniciar una llamada con un usuario (pendiente de implementación en tiempo real).
- `/group <nombre_grupo> <miembros>`: Crear un grupo con los usuarios especificados.

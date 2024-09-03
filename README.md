
# VipChatApp

VipChatApp is a simple, yet powerful web-based chat application built using the [Javalin](https://javalin.io/) framework, WebSocket technology, and other modern tools. This application allows multiple users to chat in real-time.
## Features

- **Real-time Messaging:** Chat with other users in real-time.
- **User Queue Management:** Limits the number of concurrent users and manages a queue for additional users.
- **Inactivity Timeout:** Automatically disconnects inactive users after a specified period.
- **User-Friendly Interface:** Simple and clean UI for an easy chat experience.

## Tech Stack

- **Backend:** Java, [Javalin](https://javalin.io/) framework, WebSocket API, [Jackson](https://github.com/FasterXML/jackson) for JSON parsing.
- **Frontend:** HTML5, CSS3, JavaScript.
- **Build Tool:** Maven.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6 or higher

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/VipWebChat.git
   cd VipWebChat
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Run the application:**
   ```bash
   mvn exec:java -Dexec.mainClass="localServer.VipChatApp"
   ```

4. **Access the chat application:**
   Open your web browser and navigate to `http://localhost:7777`.

### Project Structure

- **src/main/java/localServer/VipChatApp.java:** Main Java class responsible for running the server and handling WebSocket connections.
- **src/main/resources/public/index.html:** Frontend HTML file that provides the user interface.
- **src/main/resources/public/chatJSclient.js:** JavaScript client handling WebSocket communication and updating the UI.
- **src/main/resources/public/style.css:** CSS file for styling the chat interface.

### How It Works

- **WebSocket Connection:** The client establishes a WebSocket connection with the server.
- **Username Handling:** Upon connecting, the server prompts the user to enter a username. The server ensures no duplicate usernames are used.
- **Messaging:** Users can send messages to the chat, which are broadcasted to all connected users in real-time.
- **User Queue:** If the number of connected users exceeds the allowed limit, additional users are placed in a queue and are informed about their position in the queue.
- **Inactivity:** Users are automatically disconnected after 5 minutes of inactivity, freeing up space for queued users.

### Customization

You can easily customize the application by modifying the following settings in the `VipChatApp.java` file:

- **Maximum Connected Users:** Modify the `maxConnectedUsers` variable to change the number of concurrent users allowed.
- **Inactivity Timeout:** Adjust the `INACTIVITY_TIMEOUT` value to change the inactivity timeout duration.

### Contributing

Contributions are welcome! Please fork this repository, create a new branch, and submit a pull request with your changes.

### License

This project is licensed under the MIT License. See the `LICENSE` file for more details.

### Acknowledgments

- [Javalin](https://javalin.io/) - Lightweight Java and Kotlin web framework.
- [Jackson](https://github.com/FasterXML/jackson) - Java library for processing JSON.
- [j2html](https://j2html.com/) - A simple and elegant Java HTML builder.

---

Enjoy chatting!

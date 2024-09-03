// small helper function for selecting element by id
let id = id => document.getElementById(id);

let ws;
let username;

// Add event listener to the "Connect" button
id("connect").addEventListener("click", () => {
    // Get the entered username
    username = id("username").value.trim();
    if (username !== "") {
        // Establish the WebSocket connection with the chosen username
        ws = new WebSocket("ws://" + location.hostname + ":"
            + location.port + "/chats");
        ws.onopen = onSocketOpen;
        ws.onmessage = msg => updateChat(msg);
        ws.onclose = () => alert("WebSocket connection closed");
    }
});

// Add event listener to the "Logout" button
id("logout").addEventListener("click", () => {
    // Close the WebSocket connection
    ws.close();
    // Reset UI elements to initial state
    window.location.reload();
});

// Function to be called when the WebSocket connection is opened
function onSocketOpen() {
    // Send the chosen username to the server
    ws.send(JSON.stringify({ type: "username", data: username }));
    // Hide the username input field and connect button after successful connection
    id("usernameContainer").style.display = "none";
    id("chatControls").style.display = "block";
    // Hide the position label
    id("position").style.display = "none";
}

// Add event listeners to the "Send" button and input field
id("send").addEventListener("click",
    () => sendAndClear(id("message").value));
function sendAndClear(message) {
    if (message !== "") {
        let data = {
            type: "message",
            data: message
        };
        ws.send(JSON.stringify(data));
        id("message").value = "";
    }
}
function updateChat(msg) {
    id("chat").style.display = "block";
    id("chatControls").style.display = "block";
    let data = JSON.parse(msg.data);
    if (data.type === "userlist") {
        // Handle the initial message with the list of connected users
        id("userlist").innerHTML = data.data.map
        (user => "<li>" + user + "</li>").join("");
    }
    else if (data.type === "chat") {
        // Handle regular chat messages
           id("chat").insertAdjacentHTML("afterbegin", data.userMessage);
           id("position").style.display = "none";
    }
    else if (data.type === "username_taken") {
           id("usernameContainer").style.display = "block";
           id("chatControls").style.display = "none";
           id("username").value = "";
           id("username").style.display = "block";
           id("connect").style.display = "block";
           // Display an alert for a taken username
           alert(data.data);
    }
    else if ((data.type === "queue")){
           id("chatControls").style.display = "none";
           id("position").style.display = "block";
           id("position").innerHTML = data.data;
    }
    else if ((data.type === "inactivity")){
           alert(data.data);
           window.location.reload();
    }
}
package localServer;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static j2html.TagCreator.article;
import static j2html.TagCreator.attrs;
import static j2html.TagCreator.b;
import static j2html.TagCreator.p;
import static j2html.TagCreator.span;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VipChatApp {
    private static final Map<WsContext, String> ctxUsernameMap
            = new ConcurrentHashMap<>();
    private static final BlockingQueue<QueuedClient> clientQueue
            = new LinkedBlockingQueue<>();
    private static final Map<WsContext, Long> userLastActivityMap
            = new ConcurrentHashMap<>();
    private static final long INACTIVITY_TIMEOUT
            = 2 * 60 * 1000; // 5 minutes in milliseconds
    private static final int maxConnectedUsers = 4;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(7777);

        Timer inactivityTimer = new Timer(true);
        inactivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkForInactiveUsers();
            }
        },
                0, INACTIVITY_TIMEOUT / 2);

        app.ws("/chats", ws -> {
            ws.onConnect(ctx -> {
                // Send initial message to request username
                ctx.send("{\"type\":\"username\",\"data\":\"Please enter your username.\"}");
            });

            ws.onClose(ctx -> {
                String username = ctxUsernameMap.get(ctx);
                userLastActivityMap.remove(ctx);
                ctxUsernameMap.remove(ctx);
                if (username != null) {
                    //Inform other client of user disconnection
                    broadcastMessage("Server", (username + " left the chat"));
                    // Check the queue and advance users if necessary
                    advanceUsersFromQueue();
                }
            });

            ws.onMessage(ctx -> {
                userLastActivityMap.put(ctx, System.currentTimeMillis());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode messageNode = mapper.readTree(ctx.message());
                String messageType = messageNode.get("type").asText();

                if ("username".equals(messageType) && messageNode.has("data")) {
                    String username = messageNode.get("data").asText();
                    if (isUsernameTaken(username)){
                        ctx.send("{\"type\":\"username_taken\",\"data\":\"Username is already taken. " +
                                "Choose another username.\"}");
                        return;
                    }
                    if (ctxUsernameMap.size() <= maxConnectedUsers) {
                        // If there are fewer than maxConnectedUsers users, proceed as usual
                        ctxUsernameMap.put(ctx, username);
                        broadcastMessage("Server", (username + " joined the chat"));
                    } else {
                        // If there are more than maxConnectedUsers users, add the user to the queue
                        clientQueue.offer(new QueuedClient(ctx, username));
                        // Inform the user about being in the queue
                        ctx.send("{\"type\":\"queue\",\"data\":\"You are in the queue. " +
                                "Position: " + clientQueue.size() + "\"}");
                    }
                    } else {
                    // Handle regular chat messages
                    String userMessage = messageNode.get("data").asText();
                    broadcastMessage(ctxUsernameMap.get(ctx), userMessage);
                }
            });
        });
    }
    private static void broadcastMessage(String sender, String message) {
        // Send chat messages
        ctxUsernameMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(
                    Map.of(
                            "type", "chat", // Specify the message type as "chat"
                            "userMessage", createHtmlMessageFromSender(sender, message)
                    )
            );
        });
        // Send userlist updates
        ctxUsernameMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(
                    Map.of(
                            "type", "userlist", // Specify the message type as "userlist"
                            "data", ctxUsernameMap.values()
                    )
            );
        });
    }
    // Builds an HTML element with a sender-name, a message, and a timestamp
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article(
                b(sender + " says:"),
                span(attrs(".timestamp"),
                        new SimpleDateFormat("HH:mm:ss").format(new Date())),
                p(message)
        ).render();
    }
    private synchronized static void advanceUsersFromQueue() {
        while (ctxUsernameMap.size() <= maxConnectedUsers && !clientQueue.isEmpty()) {
            QueuedClient userToAdvance = clientQueue.poll();
            if (userToAdvance != null) {
                // Add the user to the chat and send a message
                ctxUsernameMap.put(userToAdvance.getContext(), userToAdvance.getUsername());
                broadcastMessage("Server", (userToAdvance.getUsername() +
                        " joined the chat"));
                // If queue is not empty, updates queue positions for all queued users
                if(!clientQueue.isEmpty()){
                int queuePos = 1;
                for (QueuedClient qu : clientQueue) {
                    qu.getContext().send("{\"type\":\"queue\",\"data\":\"You are in the queue. " +
                            "Position: " + queuePos + "\"}");
                    queuePos++;
                }
                }
            }
        }
    }
    private static synchronized boolean isUsernameTaken(String username) {
        return ctxUsernameMap.containsValue(username) ||
                clientQueue.stream()
                .anyMatch(queuedClient -> queuedClient.getUsername()
                        .equals(username));
    }
    // Method to check for inactive users and close their connections
    private static void checkForInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        userLastActivityMap.forEach((ctx, lastActivityTime) -> {
            if (currentTime - lastActivityTime > INACTIVITY_TIMEOUT) {
                String username = ctxUsernameMap.get(ctx);
                ctxUsernameMap.remove(ctx);
                userLastActivityMap.remove(ctx);
                // Optionally, notify other users or take additional actions
                ctx.send("{\"type\":\"inactivity\",\"data\":" +
                        "\"Disconnected due to inactivity\"}");
                broadcastMessage("Server", (username + " left the chat"));
                advanceUsersFromQueue();
            }
        });
    }
}
package localServer;

import io.javalin.websocket.WsContext;

class QueuedClient {
    private final WsContext context;
    private final String username;

    public QueuedClient(WsContext context, String username) {
        this.context = context;
        this.username = username;
    }

    public WsContext getContext() {
        return context;
    }

    public String getUsername() {
        return username;
    }
}

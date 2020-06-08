package com.esri.arcgis.performance.test.sst;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketClientWrapper {
    private long messageTotalBytes = 0;
    private int messageTotal = 0;
    private WebSocketClient webSocketClient;
    private long threadId;
    private boolean connectFailed = false;

    private ReconnectListener reconnectListener;

    private static int totalOpenedConnections = 0;
    private static int totalFailedConnections = 0;
    private boolean isFirstMessageReceived = false;

    public WebSocketClientWrapper(String streamServiceUrl, long requestInterval) {
        try {
            Draft_6455 draft_6455 = new Draft_6455();
            webSocketClient = new WebSocketClient(new URI(streamServiceUrl), (Draft) draft_6455) {
                @Override
                public void onMessage(String message) {
                    if (message != null) {
                        messageTotalBytes += message.length();
                        messageTotal++;
                    }
                    if (!isFirstMessageReceived) {
                        System.out.println(Thread.currentThread().getName() + " => # of messages: " + messageTotal + ", total bytes: " + messageTotalBytes);
                        isFirstMessageReceived = true;
                    }
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    threadId = Thread.currentThread().getId();
                    totalOpenedConnections++;
                    System.out.println("onOpen -> " + threadId +", " + totalOpenedConnections);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    threadId = Thread.currentThread().getId();
                    totalFailedConnections++;
                    System.out.println("onClose -> " + reason + ", remote=" + remote + ", threadId: " + threadId +", " + totalFailedConnections);
                    connectFailed =  true;
                    try {
                        Thread.sleep(requestInterval);
                    } catch (InterruptedException ex){
                        ex.printStackTrace();
                    }
                    if (reconnectListener != null) reconnectListener.reconnect(threadId);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            webSocketClient.setConnectionLostTimeout((int)requestInterval);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void connect() {
        webSocketClient.connect();
    }

    public Tuple close() {
        webSocketClient.close();
        return new Tuple(threadId, messageTotalBytes, messageTotal);
    }

    public long getThreadId() {
        return threadId;
    }

    public boolean isConnectFailed() {
        return connectFailed;
    }

    public void setReconnectListener(ReconnectListener listener) {
        reconnectListener = listener;
    }

    public void setTimeout(int timeoutInSeconds) {
        webSocketClient.setConnectionLostTimeout(timeoutInSeconds);
    }
}


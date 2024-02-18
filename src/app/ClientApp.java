package app;

import socket.BasicClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the client application.
 */
public class ClientApp {

    private static final String SERVER_HOST = "0.0.0.0";
    private static final int SERVER_PORT = 8888;
    private static final int NUM_CLIENTS = 2;
    private static final int BYTES_TO_SEND = 20;
    private static final List<BasicClient> clients = new ArrayList<>();

    public static void main(String[] args) {
        for (int i = 0; i < NUM_CLIENTS; i++) {
            BasicClient client = new BasicClient(SERVER_HOST, SERVER_PORT);
            clients.add(client);
            new Thread(() -> {
                try {
                    client.connect();
                    client.sendMessage(BYTES_TO_SEND);
                } catch (IOException e) {
                    System.err.println("Error occurred while running client: " + e.getMessage());
                } finally {
                    cleanUpResources(client);
                }
            }).start();
        }
    }

    private static synchronized void cleanUpResources(BasicClient client) {
        if (clients.contains(client)) {
            client.stop();
            clients.remove(client);
            System.out.println("Cleaned up client resources.");
        }
    }
}

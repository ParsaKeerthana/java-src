package app;

import socket.BasicServer;

import java.io.IOException;

/**
 * Entry point for the server application.
 */
public class ServerApp {

    private static final int SERVER_PORT = 8888;
    private static final String SERVER_HOST = "0.0.0.0";

    public static void main(String[] args) {
        BasicServer server = new BasicServer(SERVER_HOST, SERVER_PORT);
        addShutdownHook(server);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    /**
     * Adds a shutdown hook to the runtime environment. This hook is used to clean up resources
     * and ensure a graceful shutdown of the server when the application is terminated.
     *
     * @param server The server to shut down.
     */
    private static void addShutdownHook(final BasicServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down the server...");
            server.stop();
        }));
    }
}

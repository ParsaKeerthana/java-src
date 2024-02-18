package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicServer {
    private final String host;
    private final int port;
    private ServerSocket serverSocket;
    private final AtomicBoolean running;
    private final ExecutorService clientHandlingPool;

    public BasicServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.running = new AtomicBoolean(false);
        // Configure the ExecutorService to an appropriate number of threads for your application
        this.clientHandlingPool = Executors.newFixedThreadPool(10);
    }

    public void start() throws IOException {
        if (running.get()) {
            return; // Server already running
        }

        serverSocket = new ServerSocket(port);
        System.out.println("Server started. Listening on " + host + ":" + port);
        running.set(true);

        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                SessionHandler sessionHandler = new SessionHandler(clientSocket);
                clientHandlingPool.submit(sessionHandler);
            } catch (IOException e) {
                if (running.get()) {
                    throw new IOException("Failed to accept client connection", e);
                } else {
                    // If running is false, the server socket was closed intentionally
                    System.out.println("Server has stopped accepting new connections.");
                }
            }
        }
    }

    /**
     * Stop the server and close the server socket.
     */
    public void stop() {
        if (!running.get()) {
            return; // Server already stopped
        }
        running.set(false);
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            shutdownAndAwaitTermination(clientHandlingPool);
        } catch (IOException e) {
            System.err.println("Failed to stop server: " + e.getMessage());
        }
    }

    /**
     * Shuts down an ExecutorService in two phases, first by calling shutdown to reject incoming tasks,
     * and then calling shutdownNow, if necessary, to cancel any lingering tasks.
     */
    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}

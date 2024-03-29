package socket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SessionHandler extends Thread {
    private Socket connection;
    private volatile boolean running = true;

    public SessionHandler(Socket connection) {
        this.connection = connection;
        this.setDaemon(true); // Consider if you really want it to be a daemon thread
    }

    public void stopSession() {
        running = false; // Flag to stop the loop
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close(); // Attempt to close the connection
            }
        } catch (IOException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        } finally {
            connection = null; // Help GC by nullifying the reference
        }
    }

    @Override
    public void run() {
        if (connection == null) {
            return; // Early exit if there's no connection
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream())) {
            // Read the message length header (4 bytes)
            byte[] lengthHeader = new byte[4];
            int bytesRead = inputStream.read(lengthHeader);
            if (bytesRead != 4) {
                System.err.println("Invalid message length header");
                return;
            }
            // Convert the length header bytes to an integer
            int messageLength = ByteBuffer.wrap(lengthHeader).getInt();
            byte[] buffer = new byte[messageLength]; // 1MB buffer
            long totalBytesReceived = 0;
            long startTime = System.currentTimeMillis();

            while (running && (bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesReceived += bytesRead;
            }

            long endTime = System.currentTimeMillis();
            double transferTime = (endTime - startTime) / 1000.0;
            double gigabytesReceived = totalBytesReceived / (double)(1024 * 1024 * 1024);

            System.out.printf("Total bytes received: %.2f GB%n", gigabytesReceived);
            System.out.printf("Transfer time: %.2f seconds%n", transferTime);
        } catch (IOException e) {
            if (running) { // Only log error if we were supposed to be running
                System.err.println("Failed to receive data: " + e.getMessage());
            }
        } finally {
            stopSession(); // Ensure the session is stopped and resources are cleaned
        }
    }
}

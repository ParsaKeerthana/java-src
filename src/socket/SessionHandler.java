package socket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class SessionHandler extends Thread {
    private Socket connection;
    private volatile boolean forever = true;

    public SessionHandler(Socket connection) {
        this.connection = connection;
        this.setDaemon(true);
    }

    public void stopSession() {
        forever = false;
        if (connection != null && !connection.isClosed()) {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("Failed to close connection");
            }
        }
        connection = null;
    }

    @Override
    public void run() {
        try (BufferedInputStream inputStream =
                     new BufferedInputStream(connection.getInputStream())) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            long totalBytesReceived = 0;

            long startTime = System.currentTimeMillis();

            while (forever && (bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesReceived += bytesRead;
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Total bytes received: " + totalBytesReceived / (1024 * 1024 * 1024) + " GB");
            System.out.println("Transfer time: " + (endTime - startTime) / 1000.0 + " seconds");
        } catch (IOException e) {
            System.err.println("Failed to receive data");
        } finally {
            stopSession();
        }
    }
}
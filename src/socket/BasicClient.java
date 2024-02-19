package socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/*
 * Basic client for sending messages to a server.
 */
public class BasicClient {
    private final String host;
    private final int port;
    private Socket clt;

    /**
     * Create a new client with the given host and port.
     *
     * @param host the host to connect to
     * @param port the port to connect to
     */
    public BasicClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connect to the server at the given host and port.
     *
     * @throws IOException if an I/O error occurs when connecting
     */
    public void connect() throws IOException {
        if (this.clt != null && !clt.isClosed()) {
            return; // already connected
        }
        this.clt = new Socket(this.host, this.port);
    }

    /**
     * Stop the client and close the connection.
     */
    public void stop() {
        if (this.clt != null) {
            try {
                this.clt.close();
            } catch (IOException e) {
                System.err.println("Failed to close client connection: " + e.getMessage());
            } finally {
                this.clt = null;
            }
        }
    }

    /**
     * Send a message to the server.
     *
     * @param bytesToSend the number of GigaBytes to send to the server
     * @throws IOException if an I/O error occurs when sending or if not connected
     */
    public void sendMessage(int bytesToSend) throws IOException {
        if (clt == null || clt.isClosed()) {
            throw new IOException("No connection to server");
        }

        byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
        long totalBytesSent = 0;
        long totalBytesToSend = Math.multiplyExact((long) bytesToSend, 1024 * 1024 * 1024); // to avoid overflow
        long startTime = System.currentTimeMillis();

        try (OutputStream outputStream = clt.getOutputStream()) {
            byte[] lengthHeader = ByteBuffer.allocate(4).putInt(buffer.length).array();
            // Send the header first
            outputStream.write(lengthHeader);
            while (totalBytesSent < totalBytesToSend) {
                outputStream.write(buffer, 0, (int)Math.min(buffer.length, totalBytesToSend - totalBytesSent));
                totalBytesSent += Math.min(buffer.length, totalBytesToSend - totalBytesSent);
            }
        } catch (IOException e) {
            throw new IOException("Failed to send message to server", e);
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("Sent " + totalBytesSent / (1024 * 1024 * 1024) + " GB");
            System.out.println("Transfer time: " + (endTime - startTime) / 1000.0 + " seconds");

            // Stop the client after sending the message
            stop();
        }
    }
}

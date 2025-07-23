package net.minehub.velocity.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerPingUtil {

    /**
     * Ping a server to check if it's online
     * @param host Server hostname/IP
     * @param port Server port
     * @param timeoutMs Timeout in milliseconds
     * @return true if server is reachable, false otherwise
     */
    public static boolean pingServer(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (SocketTimeoutException e) {
            // Server is unreachable or too slow
            return false;
        } catch (IOException e) {
            // Connection failed
            return false;
        } catch (Exception e) {
            // Other errors
            return false;
        }
    }

    /**
     * Asynchronously ping a server
     * @param host Server hostname/IP
     * @param port Server port
     * @param timeoutMs Timeout in milliseconds
     * @return CompletableFuture<Boolean> indicating if server is online
     */
    public static CompletableFuture<Boolean> pingServerAsync(String host, int port, int timeoutMs) {
        return CompletableFuture.supplyAsync(() -> pingServer(host, port, timeoutMs));
    }

    /**
     * Ping server with default timeout of 5 seconds
     * @param host Server hostname/IP
     * @param port Server port
     * @return true if server is reachable, false otherwise
     */
    public static boolean pingServer(String host, int port) {
        return pingServer(host, port, 5000);
    }

    /**
     * Ping multiple servers concurrently
     * @param servers Array of server addresses in format "host:port"
     * @param timeoutMs Timeout in milliseconds per server
     * @return CompletableFuture that completes when all pings are done
     */
    public static CompletableFuture<Boolean[]> pingMultipleServers(String[] servers, int timeoutMs) {
        CompletableFuture<Boolean>[] futures = new CompletableFuture[servers.length];

        for (int i = 0; i < servers.length; i++) {
            String[] parts = servers[i].split(":");
            if (parts.length == 2) {
                try {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    futures[i] = pingServerAsync(host, port, timeoutMs);
                } catch (NumberFormatException e) {
                    futures[i] = CompletableFuture.completedFuture(false);
                }
            } else {
                futures[i] = CompletableFuture.completedFuture(false);
            }
        }

        return CompletableFuture.allOf(futures)
            .thenApply(v -> {
                Boolean[] results = new Boolean[futures.length];
                for (int i = 0; i < futures.length; i++) {
                    try {
                        results[i] = futures[i].get(1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        results[i] = false;
                    }
                }
                return results;
            });
    }

    /**
     * Get ping time to a server
     * @param host Server hostname/IP
     * @param port Server port
     * @param timeoutMs Timeout in milliseconds
     * @return Ping time in milliseconds, -1 if unreachable
     */
    public static long getPingTime(String host, int port, int timeoutMs) {
        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            long endTime = System.currentTimeMillis();
            return endTime - startTime;
        } catch (Exception e) {
            return -1;
        }
    }
}
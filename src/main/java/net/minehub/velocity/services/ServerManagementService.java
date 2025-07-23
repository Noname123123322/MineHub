package net.minehub.velocity.services;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.minehub.velocity.database.DatabaseManager;
import net.minehub.velocity.utils.ServerPingUtil;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerManagementService {
    private final ProxyServer proxyServer;
    private final DatabaseManager databaseManager;
    private final Logger logger;
    private final ConcurrentMap<String, net.minehub.velocity.models.ServerInfo> managedServers;

    public ServerManagementService(ProxyServer proxyServer, DatabaseManager databaseManager, Logger logger) {
        this.proxyServer = proxyServer;
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.managedServers = new ConcurrentHashMap<>();
    }

    public CompletableFuture<Boolean> addServer(String name, String host, int port, UUID ownerUuid, String ownerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if server already exists
                if (proxyServer.getServer(name).isPresent()) {
                    logger.warn("Server {} already exists in proxy", name);
                    return false;
                }

                // Create server info
                net.minehub.velocity.models.ServerInfo serverInfo = new net.minehub.velocity.models.ServerInfo(
                    name, host, port, ownerUuid, ownerName
                );

                // Test server connectivity
                boolean isOnline = ServerPingUtil.pingServer(host, port, 5000);
                serverInfo.setOnline(isOnline);

                // Add to database
                databaseManager.addServer(serverInfo);

                // Register with proxy
                ServerInfo velocityServerInfo = new ServerInfo(name, new InetSocketAddress(host, port));
                RegisteredServer registeredServer = proxyServer.registerServer(velocityServerInfo);

                // Store in managed servers
                managedServers.put(name, serverInfo);

                logger.info("Server {} added successfully (Online: {})", name, isOnline);
                return true;

            } catch (SQLException e) {
                logger.error("Failed to add server {} to database", name, e);
                return false;
            } catch (Exception e) {
                logger.error("Failed to add server {}", name, e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> removeServer(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Remove from proxy
                Optional<RegisteredServer> server = proxyServer.getServer(name);
                if (server.isPresent()) {
                    proxyServer.unregisterServer(server.get());
                }

                // Remove from database
                databaseManager.removeServer(name);

                // Remove from managed servers
                managedServers.remove(name);

                logger.info("Server {} removed successfully", name);
                return true;

            } catch (SQLException e) {
                logger.error("Failed to remove server {} from database", name, e);
                return false;
            } catch (Exception e) {
                logger.error("Failed to remove server {}", name, e);
                return false;
            }
        });
    }

    public void loadServersFromDatabase() {
        try {
            List<net.minehub.velocity.models.ServerInfo> servers = databaseManager.getAllServers();

            for (net.minehub.velocity.models.ServerInfo serverInfo : servers) {
                try {
                    // Register with proxy
                    ServerInfo velocityServerInfo = new ServerInfo(
                        serverInfo.getName(), 
                        new InetSocketAddress(serverInfo.getHost(), serverInfo.getPort())
                    );
                    proxyServer.registerServer(velocityServerInfo);

                    // Store in managed servers
                    managedServers.put(serverInfo.getName(), serverInfo);

                    logger.debug("Loaded server: {}", serverInfo.getName());

                } catch (Exception e) {
                    logger.error("Failed to load server {}", serverInfo.getName(), e);
                }
            }

            logger.info("Loaded {} servers from database", servers.size());

        } catch (SQLException e) {
            logger.error("Failed to load servers from database", e);
        }
    }

    public CompletableFuture<Void> updateAllServerStatuses() {
        return CompletableFuture.runAsync(() -> {
            for (net.minehub.velocity.models.ServerInfo serverInfo : managedServers.values()) {
                try {
                    boolean isOnline = ServerPingUtil.pingServer(
                        serverInfo.getHost(), 
                        serverInfo.getPort(), 
                        3000
                    );

                    if (serverInfo.isOnline() != isOnline) {
                        serverInfo.setOnline(isOnline);
                        databaseManager.updateServerStatus(serverInfo.getName(), isOnline);

                        logger.debug("Server {} status updated: {}", 
                            serverInfo.getName(), 
                            isOnline ? "Online" : "Offline"
                        );
                    }

                } catch (Exception e) {
                    logger.error("Failed to update status for server {}", serverInfo.getName(), e);
                }
            }
        });
    }

    public CompletableFuture<List<net.minehub.velocity.models.ServerInfo>> getOfflineServers(int hoursOffline) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<net.minehub.velocity.models.ServerInfo> offlineServers = 
                    databaseManager.getOfflineServers(hoursOffline);

                logger.debug("Found {} servers offline for more than {} hours", 
                    offlineServers.size(), hoursOffline);

                return offlineServers;

            } catch (SQLException e) {
                logger.error("Failed to get offline servers", e);
                return List.of();
            }
        });
    }

    public CompletableFuture<Void> cleanupOfflineServers(int hoursOffline) {
        return CompletableFuture.runAsync(() -> {
            try {
                List<net.minehub.velocity.models.ServerInfo> offlineServers = 
                    databaseManager.getOfflineServers(hoursOffline);

                for (net.minehub.velocity.models.ServerInfo server : offlineServers) {
                    removeServer(server.getName()).join();
                    logger.info("Cleaned up offline server: {} (offline for {}+ hours)", 
                        server.getName(), hoursOffline);
                }

                if (!offlineServers.isEmpty()) {
                    logger.info("Cleaned up {} offline servers", offlineServers.size());
                }

            } catch (SQLException e) {
                logger.error("Failed to cleanup offline servers", e);
            }
        });
    }

    public List<net.minehub.velocity.models.ServerInfo> getAllManagedServers() {
        return List.copyOf(managedServers.values());
    }

    public Optional<net.minehub.velocity.models.ServerInfo> getServerInfo(String name) {
        return Optional.ofNullable(managedServers.get(name));
    }

    public int getServerCountByOwner(UUID ownerUuid) {
        try {
            return databaseManager.getServerCountByOwner(ownerUuid);
        } catch (SQLException e) {
            logger.error("Failed to get server count for owner {}", ownerUuid, e);
            return 0;
        }
    }

    public boolean isServerOnline(String serverName) {
        net.minehub.velocity.models.ServerInfo serverInfo = managedServers.get(serverName);
        return serverInfo != null && serverInfo.isOnline();
    }
}
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
                if (proxyServer.getServer(name).isPresent()) {
                    logger.warn("Server '{}' already exists", name);
                    return false;
                }

                var info = new net.minehub.velocity.models.ServerInfo(name, host, port, ownerUuid, ownerName);
                info.setOnline(ServerPingUtil.pingServer(host, port, 3000));

                databaseManager.addServer(info);

                ServerInfo serverInfo = new ServerInfo(name, new InetSocketAddress(host, port));
                proxyServer.registerServer(serverInfo);

                managedServers.put(name, info);
                logger.info("Server '{}' added successfully", name);

                return true;

            } catch (Exception e) {
                logger.error("Failed to add server '{}'", name, e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> removeServer(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                proxyServer.getServer(name)
                        .map(RegisteredServer::getServerInfo)
                        .ifPresent(proxyServer::unregisterServer);

                databaseManager.removeServer(name);
                managedServers.remove(name);
                logger.info("Server '{}' removed", name);
                return true;
            } catch (Exception e) {
                logger.error("Failed to remove server '{}'", name, e);
                return false;
            }
        });
    }

    public void loadServersFromDatabase() {
        try {
            for (var s : databaseManager.getAllServers()) {
                var si = new ServerInfo(s.getName(), new InetSocketAddress(s.getHost(), s.getPort()));
                proxyServer.registerServer(si);
                managedServers.put(s.getName(), s);
            }
            logger.info("Loaded servers from database");
        } catch (SQLException e) {
            logger.error("Failed to load servers", e);
        }
    }

    public CompletableFuture<Void> cleanupOfflineServers(int hoursOffline) {
        return CompletableFuture.runAsync(() -> {
            try {
                var list = databaseManager.getOfflineServers(hoursOffline);
                list.forEach(s -> {
                    removeServer(s.getName()).join();
                    logger.info("Removed inactive server '{}'", s.getName());
                });
            } catch (SQLException e) {
                logger.error("Cleanup failed", e);
            }
        });
    }

    public boolean isServerOnline(String name) {
        return Optional.ofNullable(managedServers.get(name))
                .map(net.minehub.velocity.models.ServerInfo::isOnline)
                .orElse(false);
    }

    public Optional<net.minehub.velocity.models.ServerInfo> getServerInfo(String name) {
        return Optional.ofNullable(managedServers.get(name));
    }

    public List<net.minehub.velocity.models.ServerInfo> getAllManagedServers() {
        return List.copyOf(managedServers.values());
    }

    public int getServerCountByOwner(UUID ownerUuid) {
        try {
            return databaseManager.getServerCountByOwner(ownerUuid);
        } catch (SQLException e) {
            logger.error("Couldn't count servers for owner {}", ownerUuid);
            return 0;
        }
    }
}

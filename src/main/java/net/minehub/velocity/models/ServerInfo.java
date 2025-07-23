package net.minehub.velocity.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServerInfo {
    private String name;
    private String host;
    private int port;
    private UUID ownerUuid;
    private String ownerName;
    private String description;
    private int maxPlayers;
    private String version;
    private boolean isOnline;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;

    public ServerInfo(String name, String host, int port, UUID ownerUuid, String ownerName) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.description = "";
        this.maxPlayers = 20;
        this.version = "Unknown";
        this.isOnline = false;
        this.createdAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }

    // Getters
    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getOwnerName() { return ownerName; }
    public String getDescription() { return description; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getVersion() { return version; }
    public boolean isOnline() { return isOnline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastSeen() { return lastSeen; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setDescription(String description) { this.description = description; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public void setVersion(String version) { this.version = version; }
    public void setOnline(boolean online) { this.isOnline = online; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public String getAddress() {
        return host + ":" + port;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", ownerName='" + ownerName + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ServerInfo that = (ServerInfo) obj;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
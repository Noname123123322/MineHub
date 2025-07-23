package net.minehub.velocity.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.minehub.velocity.config.ConfigManager;
import net.minehub.velocity.models.ServerInfo;
import org.slf4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private final ConfigManager configManager;
    private final Logger logger;
    private HikariDataSource dataSource;

    public DatabaseManager(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    public void initialize() throws SQLException {
        setupConnectionPool();
        createTables();
        logger.info("Database initialized successfully");
    }

    private void setupConnectionPool() {
        HikariConfig config = new HikariConfig();

        String host = configManager.getString("database.host", "localhost");
        int port = configManager.getInt("database.port", 3306);
        String database = configManager.getString("database.database", "minehub");
        String username = configManager.getString("database.username", "minehub");
        String password = configManager.getString("database.password", "password");
        int poolSize = configManager.getInt("database.pool-size", 10);

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);
    }

    private void createTables() throws SQLException {
        String createServersTable = """
            CREATE TABLE IF NOT EXISTS minehub_servers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL UNIQUE,
                host VARCHAR(255) NOT NULL,
                port INT NOT NULL,
                owner_uuid VARCHAR(36) NOT NULL,
                owner_name VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_online BOOLEAN DEFAULT FALSE,
                description TEXT,
                max_players INT DEFAULT 20,
                version VARCHAR(50) DEFAULT 'Unknown',
                INDEX idx_owner_uuid (owner_uuid),
                INDEX idx_last_seen (last_seen),
                INDEX idx_is_online (is_online)
            )""";

        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS minehub_players (
                uuid VARCHAR(36) PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_server VARCHAR(255),
                join_count INT DEFAULT 1,
                INDEX idx_username (username),
                INDEX idx_last_join (last_join)
            )""";

        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createServersTable);
                stmt.execute(createPlayersTable);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void addServer(ServerInfo serverInfo) throws SQLException {
        String sql = """
            INSERT INTO minehub_servers (name, host, port, owner_uuid, owner_name, description, max_players, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, serverInfo.getName());
            stmt.setString(2, serverInfo.getHost());
            stmt.setInt(3, serverInfo.getPort());
            stmt.setString(4, serverInfo.getOwnerUuid().toString());
            stmt.setString(5, serverInfo.getOwnerName());
            stmt.setString(6, serverInfo.getDescription());
            stmt.setInt(7, serverInfo.getMaxPlayers());
            stmt.setString(8, serverInfo.getVersion());

            stmt.executeUpdate();
        }
    }

    public void removeServer(String serverName) throws SQLException {
        String sql = "DELETE FROM minehub_servers WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, serverName);
            stmt.executeUpdate();
        }
    }

    public void updateServerStatus(String serverName, boolean isOnline) throws SQLException {
        String sql = "UPDATE minehub_servers SET is_online = ?, last_seen = CURRENT_TIMESTAMP WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isOnline);
            stmt.setString(2, serverName);
            stmt.executeUpdate();
        }
    }

    public List<ServerInfo> getAllServers() throws SQLException {
        String sql = "SELECT * FROM minehub_servers ORDER BY created_at DESC";
        List<ServerInfo> servers = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ServerInfo server = new ServerInfo(
                    rs.getString("name"),
                    rs.getString("host"),
                    rs.getInt("port"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    rs.getString("owner_name")
                );

                server.setDescription(rs.getString("description"));
                server.setMaxPlayers(rs.getInt("max_players"));
                server.setVersion(rs.getString("version"));
                server.setOnline(rs.getBoolean("is_online"));
                server.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                server.setLastSeen(rs.getTimestamp("last_seen").toLocalDateTime());

                servers.add(server);
            }
        }

        return servers;
    }

    public List<ServerInfo> getOfflineServers(int hoursOffline) throws SQLException {
        String sql = "SELECT * FROM minehub_servers WHERE last_seen < (NOW() - INTERVAL ? HOUR)";
        List<ServerInfo> servers = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hoursOffline);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ServerInfo server = new ServerInfo(
                        rs.getString("name"),
                        rs.getString("host"),
                        rs.getInt("port"),
                        UUID.fromString(rs.getString("owner_uuid")),
                        rs.getString("owner_name")
                    );

                    server.setDescription(rs.getString("description"));
                    server.setMaxPlayers(rs.getInt("max_players"));
                    server.setVersion(rs.getString("version"));
                    server.setOnline(rs.getBoolean("is_online"));
                    server.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    server.setLastSeen(rs.getTimestamp("last_seen").toLocalDateTime());

                    servers.add(server);
                }
            }
        }

        return servers;
    }

    public int getServerCountByOwner(UUID ownerUuid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM minehub_servers WHERE owner_uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ownerUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public void updatePlayerData(UUID playerUuid, String username, String lastServer) throws SQLException {
        String sql = """
            INSERT INTO minehub_players (uuid, username, last_server, last_join, join_count)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, 1)
            ON DUPLICATE KEY UPDATE
            username = VALUES(username),
            last_server = VALUES(last_server),
            last_join = CURRENT_TIMESTAMP,
            join_count = join_count + 1
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, lastServer);
            stmt.executeUpdate();
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
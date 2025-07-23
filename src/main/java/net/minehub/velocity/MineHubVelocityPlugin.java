package net.minehub.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.minehub.velocity.commands.AddServerCommand;
import net.minehub.velocity.commands.MineHubCommand;
import net.minehub.velocity.commands.RemoveServerCommand;
import net.minehub.velocity.config.ConfigManager;
import net.minehub.velocity.database.DatabaseManager;
import net.minehub.velocity.listeners.PlayerJoinListener;
import net.minehub.velocity.services.ServerManagementService;
import net.minehub.velocity.tasks.ServerCleanupTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
    id = "minehub-velocity",
    name = "MineHub Velocity Plugin",
    version = "1.0.0",
    description = "Dynamic server management plugin for MineHub network",
    authors = {"MineHub Team"}
)
public class MineHubVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ServerManagementService serverManagementService;
    private ServerCleanupTask cleanupTask;

    @Inject
    public MineHubVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing MineHub Velocity Plugin...");

        try {
            // Initialize configuration
            configManager = new ConfigManager(dataDirectory);
            configManager.loadConfig();

            // Initialize database
            databaseManager = new DatabaseManager(configManager, logger);
            databaseManager.initialize();

            // Initialize services
            serverManagementService = new ServerManagementService(server, databaseManager, logger);

            // Register commands
            registerCommands();

            // Register event listeners
            registerListeners();

            // Start cleanup task
            startCleanupTask();

            // Load existing servers from database
            serverManagementService.loadServersFromDatabase();

            logger.info("MineHub Velocity Plugin initialized successfully!");

        } catch (Exception e) {
            logger.error("Failed to initialize MineHub Velocity Plugin", e);
            throw new RuntimeException("Plugin initialization failed", e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutting down MineHub Velocity Plugin...");

        if (cleanupTask != null) {
            cleanupTask.stop();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        logger.info("MineHub Velocity Plugin shutdown complete.");
    }

    private void registerCommands() {
        // Register main hub command
        server.getCommandManager().register("minehub", new MineHubCommand(serverManagementService));
        server.getCommandManager().register("hub", new MineHubCommand(serverManagementService));

        // Register server management commands
        server.getCommandManager().register("addserver", new AddServerCommand(serverManagementService));
        server.getCommandManager().register("removeserver", new RemoveServerCommand(serverManagementService));

        logger.info("Commands registered successfully");
    }

    private void registerListeners() {
        server.getEventManager().register(this, new PlayerJoinListener(serverManagementService));
        logger.info("Event listeners registered successfully");
    }

    private void startCleanupTask() {
        cleanupTask = new ServerCleanupTask(serverManagementService, logger);

        // Schedule cleanup task to run every hour
        server.getScheduler()
            .buildTask(this, cleanupTask)
            .repeat(1L, TimeUnit.HOURS)
            .schedule();

        logger.info("Server cleanup task started (runs every hour)");
    }

    // Getters for dependency injection
    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ServerManagementService getServerManagementService() {
        return serverManagementService;
    }
}
package net.minehub.velocity.tasks;

import net.minehub.velocity.services.ServerManagementService;
import org.slf4j.Logger;

public class ServerCleanupTask implements Runnable {
    private final ServerManagementService serverManagementService;
    private final Logger logger;
    private volatile boolean running = true;

    public ServerCleanupTask(ServerManagementService serverManagementService, Logger logger) {
        this.serverManagementService = serverManagementService;
        this.logger = logger;
    }

    @Override
    public void run() {
        if (!running) return;

        try {
            logger.debug("Starting server cleanup task...");

            // Update all server statuses first
            serverManagementService.updateAllServerStatuses().join();

            // Clean up servers offline for more than 72 hours (3 days)
            int hoursOffline = 72; // TODO: Get from config
            serverManagementService.cleanupOfflineServers(hoursOffline).join();

            logger.debug("Server cleanup task completed successfully");

        } catch (Exception e) {
            logger.error("Error during server cleanup task", e);
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
package net.minehub.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import net.minehub.velocity.services.ServerManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerJoinListener {
    private final ServerManagementService serverManagementService;
    private final Logger logger = LoggerFactory.getLogger(PlayerJoinListener.class);

    public PlayerJoinListener(ServerManagementService serverManagementService) {
        this.serverManagementService = serverManagementService;
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();

        // Log player join for statistics
        logger.info("Player {} ({}) connected to the network", 
            player.getUsername(), player.getUniqueId());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        // Update player data in database
        // This is done asynchronously to avoid blocking the main thread
        // TODO: Implement database update for player tracking

        logger.debug("Player {} connected to server {}", 
            player.getUsername(), serverName);
    }
}
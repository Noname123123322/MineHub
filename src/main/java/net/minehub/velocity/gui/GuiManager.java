package net.minehub.velocity.gui;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minehub.velocity.models.ServerInfo;
import net.minehub.velocity.services.ServerManagementService;

import java.util.List;

/**
 * GUI Manager for MineHub
 * Note: This is a placeholder for future GUI implementation.
 * Velocity doesn't have built-in inventory GUI support like Bukkit/Spigot.
 * This would require a separate hub server with GUI capabilities.
 */
public class GuiManager {
    private final ServerManagementService serverManagementService;

    public GuiManager(ServerManagementService serverManagementService) {
        this.serverManagementService = serverManagementService;
    }

    /**
     * Opens the server selector GUI for a player
     * Note: This is a text-based implementation since Velocity doesn't support GUIs directly
     * @param player The player to show the GUI to
     */
    public void openServerSelector(Player player) {
        List<ServerInfo> servers = serverManagementService.getAllManagedServers();

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("           MineHub Server Selector").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        if (servers.isEmpty()) {
            player.sendMessage(Component.text("  No servers are currently available.").color(NamedTextColor.RED));
        } else {
            int index = 1;
            for (ServerInfo server : servers) {
                Component statusIcon = server.isOnline() 
                    ? Component.text("✓").color(NamedTextColor.GREEN)
                    : Component.text("✗").color(NamedTextColor.RED);

                Component serverLine = Component.text("  " + index + ". ")
                    .color(NamedTextColor.GRAY)
                    .append(statusIcon)
                    .append(Component.text(" " + server.getName()).color(NamedTextColor.WHITE))
                    .append(Component.text(" (" + server.getAddress() + ")").color(NamedTextColor.GRAY));

                player.sendMessage(serverLine);

                if (!server.getDescription().isEmpty()) {
                    player.sendMessage(Component.text("     " + server.getDescription()).color(NamedTextColor.DARK_GRAY));
                }

                index++;
            }
        }

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  Use /minehub connect <server> to join a server").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
    }

    /**
     * Shows server management GUI for server owners
     * @param player The player to show the GUI to
     */
    public void openServerManagement(Player player) {
        List<ServerInfo> ownedServers = serverManagementService.getAllManagedServers()
            .stream()
            .filter(server -> server.getOwnerUuid().equals(player.getUniqueId()))
            .toList();

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("          Your Servers").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.GRAY));
        player.sendMessage(Component.empty());

        if (ownedServers.isEmpty()) {
            player.sendMessage(Component.text("  You don't own any servers yet.").color(NamedTextColor.GRAY));
            player.sendMessage(Component.text("  Use /addserver <name> <host> <port> to add one!").color(NamedTextColor.YELLOW));
        } else {
            for (ServerInfo server : ownedServers) {
                Component statusIcon = server.isOnline() 
                    ? Component.text("●").color(NamedTextColor.GREEN)
                    : Component.text("●").color(NamedTextColor.RED);

                player.sendMessage(Component.text("  ")
                    .append(statusIcon)
                    .append(Component.text(" " + server.getName()).color(NamedTextColor.WHITE))
                    .append(Component.text(" - " + server.getAddress()).color(NamedTextColor.GRAY)));

                if (!server.getDescription().isEmpty()) {
                    player.sendMessage(Component.text("    " + server.getDescription()).color(NamedTextColor.DARK_GRAY));
                }
            }
        }

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  Commands:").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("  /addserver <name> <host> <port> - Add a new server").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("  /removeserver <name> - Remove a server").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.GRAY));
        player.sendMessage(Component.empty());
    }
}
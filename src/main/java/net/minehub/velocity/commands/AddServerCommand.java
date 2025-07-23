package net.minehub.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minehub.velocity.services.ServerManagementService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AddServerCommand implements SimpleCommand {
    private final ServerManagementService serverManagementService;

    public AddServerCommand(ServerManagementService serverManagementService) {
        this.serverManagementService = serverManagementService;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /addserver <name> <host> <port> [description]", NamedTextColor.RED));
            return;
        }

        // Check permission
        if (!player.hasPermission("minehub.addserver")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        String serverName = args[0];
        String host = args[1];
        int port;

        try {
            port = Integer.parseInt(args[2]);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid port number! Must be between 1 and 65535.", NamedTextColor.RED));
            return;
        }

        // Check if player has reached server limit
        int currentServerCount = serverManagementService.getServerCountByOwner(player.getUniqueId());
        int maxServers = 5; // TODO: Get from config

        if (currentServerCount >= maxServers) {
            player.sendMessage(Component.text("You have reached the maximum number of servers (" + maxServers + ")!", NamedTextColor.RED));
            return;
        }

        // Validate server name
        if (!isValidServerName(serverName)) {
            player.sendMessage(Component.text("Invalid server name! Use only letters, numbers, hyphens, and underscores.", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Adding server " + serverName + "...", NamedTextColor.YELLOW));

        // Add server asynchronously
        serverManagementService.addServer(serverName, host, port, player.getUniqueId(), player.getUsername())
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(Component.text("Server '" + serverName + "' has been added successfully!", NamedTextColor.GREEN));
                    player.sendMessage(Component.text("Players can now connect using /server " + serverName, NamedTextColor.GRAY));
                } else {
                    player.sendMessage(Component.text("Failed to add server '" + serverName + "'. It may already exist or there was a database error.", NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                player.sendMessage(Component.text("An error occurred while adding the server: " + throwable.getMessage(), NamedTextColor.RED));
                return null;
            });
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("minehub.addserver");
    }

    private boolean isValidServerName(String name) {
        if (name.length() < 3 || name.length() > 32) {
            return false;
        }
        return name.matches("^[a-zA-Z0-9_-]+$");
    }
}
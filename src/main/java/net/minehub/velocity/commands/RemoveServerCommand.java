package net.minehub.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minehub.velocity.models.ServerInfo;
import net.minehub.velocity.services.ServerManagementService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RemoveServerCommand implements SimpleCommand {
    private final ServerManagementService serverManagementService;

    public RemoveServerCommand(ServerManagementService serverManagementService) {
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

        if (args.length != 1) {
            player.sendMessage(Component.text("Usage: /removeserver <name>", NamedTextColor.RED));
            return;
        }

        // Check permission
        if (!player.hasPermission("minehub.removeserver")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        String serverName = args[0];

        // Check if server exists
        Optional<ServerInfo> serverInfo = serverManagementService.getServerInfo(serverName);
        if (serverInfo.isEmpty()) {
            player.sendMessage(Component.text("Server '" + serverName + "' not found!", NamedTextColor.RED));
            return;
        }

        // Check if player owns the server or has admin permission
        if (!serverInfo.get().getOwnerUuid().equals(player.getUniqueId()) && 
            !player.hasPermission("minehub.removeserver.others")) {
            player.sendMessage(Component.text("You can only remove servers that you own!", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Removing server " + serverName + "...", NamedTextColor.YELLOW));

        // Remove server asynchronously
        serverManagementService.removeServer(serverName)
            .thenAccept(success -> {
                if (success) {
                    player.sendMessage(Component.text("Server '" + serverName + "' has been removed successfully!", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Failed to remove server '" + serverName + "'.", NamedTextColor.RED));
                }
            })
            .exceptionally(throwable -> {
                player.sendMessage(Component.text("An error occurred while removing the server: " + throwable.getMessage(), NamedTextColor.RED));
                return null;
            });
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            // Suggest server names owned by the player
            if (invocation.source() instanceof Player) {
                Player player = (Player) invocation.source();
                return CompletableFuture.completedFuture(
                    serverManagementService.getAllManagedServers().stream()
                        .filter(server -> server.getOwnerUuid().equals(player.getUniqueId()) || 
                                        player.hasPermission("minehub.removeserver.others"))
                        .map(ServerInfo::getName)
                        .filter(name -> name.toLowerCase().startsWith(invocation.arguments()[0].toLowerCase()))
                        .collect(Collectors.toList())
                );
            }
        }

        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("minehub.removeserver");
    }
}
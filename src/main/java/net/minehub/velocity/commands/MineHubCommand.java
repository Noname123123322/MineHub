package net.minehub.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minehub.velocity.models.ServerInfo;
import net.minehub.velocity.services.ServerManagementService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MineHubCommand implements SimpleCommand {

    private final ServerManagementService serverManagementService;

    public MineHubCommand(ServerManagementService serverManagementService) {
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

        if (args.length == 0) {
            showServerList(player);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "list":
                showServerList(player);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /minehub info <server>", NamedTextColor.RED));
                    break;
                }
                showServerInfo(player, args[1]);
                break;
            case "connect":
            case "join":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /minehub connect <server>", NamedTextColor.RED));
                    break;
                }
                connectToServer(player, args[1]);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                connectToServer(player, subCommand);
                break;
        }
    }

    private void showServerList(Player player) {
        List<ServerInfo> servers = serverManagementService.getAllManagedServers();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("MineHub Network - Available Servers")
            .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("=" + "=".repeat(40)).color(NamedTextColor.GRAY));
        if (servers.isEmpty()) {
            player.sendMessage(Component.text("No servers are currently available.", NamedTextColor.GRAY));
        } else {
            for (ServerInfo server : servers) {
                Component statusIcon = server.isOnline()
                    ? Component.text("●").color(NamedTextColor.GREEN)
                    : Component.text("●").color(NamedTextColor.RED);
                Component serverLine = Component.empty()
                    .append(statusIcon)
                    .append(Component.text(" " + server.getName()).color(NamedTextColor.WHITE))
                    .append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text(server.getDescription().isEmpty() ? "No description" : server.getDescription()).color(NamedTextColor.GRAY));
                player.sendMessage(serverLine);
            }
        }
        player.sendMessage(Component.text("=" + "=".repeat(40)).color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("Use /minehub connect <server> to join a server").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.empty());
    }

    private void showServerInfo(Player player, String serverName) {
        Optional<ServerInfo> serverInfo = serverManagementService.getServerInfo(serverName);
        if (serverInfo.isEmpty()) {
            player.sendMessage(Component.text("Server '" + serverName + "' not found!", NamedTextColor.RED));
            return;
        }
        ServerInfo server = serverInfo.get();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Server Information: " + server.getName())
            .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("=" + "=".repeat(30)).color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("Status: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(server.isOnline() ? "Online" : "Offline")
            .color(server.isOnline() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        player.sendMessage(Component.text("Address: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(server.getAddress()).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Owner: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(server.getOwnerName()).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Description: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(server.getDescription().isEmpty() ? "No description" : server.getDescription()).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Max Players: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(String.valueOf(server.getMaxPlayers())).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Version: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(server.getVersion()).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.empty());
    }

    private void connectToServer(Player player, String serverName) {
        Optional<RegisteredServer> server = player.getCurrentServer().map(conn -> conn.getServer().getServer().getAllServers()
            .stream()
            .filter(s -> s.getServerInfo().getName().equalsIgnoreCase(serverName))
            .findFirst()).orElse(
            player.getProxy().getAllServers()
                .stream()
                .filter(s -> s.getServerInfo().getName().equalsIgnoreCase(serverName))
                .findFirst()
        );

        if (server.isEmpty()) {
            player.sendMessage(Component.text("Server '" + serverName + "' not found!", NamedTextColor.RED));
            return;
        }

        // Check if server is online
        if (!serverManagementService.isServerOnline(serverName)) {
            player.sendMessage(Component.text("Server '" + serverName + "' is currently offline!", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Connecting to " + serverName + "...", NamedTextColor.YELLOW));
        player.createConnectionRequest(server.get()).fireAndForget();
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("MineHub Commands")
            .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("=" + "=".repeat(20)).color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("/minehub").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Show server list").color(NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/minehub list").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Show server list").color(NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/minehub connect <server>").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Connect to a server").color(NamedTextColor.GRAY)));
        player.sendMessage(Component.text("/minehub info <server>").color(NamedTextColor.YELLOW)
            .append(Component.text(" - Show server information").color(NamedTextColor.GRAY)));
        if (player.hasPermission("minehub.addserver")) {
            player.sendMessage(Component.text("/addserver <name> <ip> <port>").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Add a new server").color(NamedTextColor.GRAY)));
        }
        if (player.hasPermission("minehub.removeserver")) {
            player.sendMessage(Component.text("/removeserver <name>").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Remove a server").color(NamedTextColor.GRAY)));
        }
        player.sendMessage(Component.empty());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            List<String> suggestions = List.of("list", "info", "connect", "help");
            return CompletableFuture.completedFuture(
                suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList()
            );
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("connect"))) {
            return CompletableFuture.completedFuture(
                serverManagementService.getAllManagedServers().stream()
                    .map(ServerInfo::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList()
            );
        }
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // Everyone can use basic hub commands
    }
}

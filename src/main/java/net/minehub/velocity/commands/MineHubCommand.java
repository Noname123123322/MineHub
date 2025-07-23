package net.minehub.velocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
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

    private final ProxyServer proxyServer;
    private final ServerManagementService serverManagementService;

    public MineHubCommand(ProxyServer proxyServer, ServerManagementService serverManagementService) {
        this.proxyServer = proxyServer;
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
                } else {
                    showServerInfo(player, args[1]);
                }
                break;
            case "connect":
            case "join":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /minehub connect <server>", NamedTextColor.RED));
                } else {
                    connectToServer(player, args[1]);
                }
                break;
            case "help":
                showHelp(player);
                break;
            default:
                connectToServer(player, subCommand);
                break;
        }
    }

    private void connectToServer(Player player, String serverName) {
        Optional<RegisteredServer> target = proxyServer.getAllServers()
            .stream()
            .filter(s -> s.getServerInfo().getName().equalsIgnoreCase(serverName))
            .findFirst();

        if (target.isEmpty()) {
            player.sendMessage(Component.text("Server '" + serverName + "' not found!", NamedTextColor.RED));
            return;
        }

        if (!serverManagementService.isServerOnline(serverName)) {
            player.sendMessage(Component.text("Server '" + serverName + "' is offline!", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Connecting to " + serverName + "...", NamedTextColor.YELLOW));
        player.createConnectionRequest(target.get()).fireAndForget();
    }

    private void showServerList(Player player) {
        List<ServerInfo> servers = serverManagementService.getAllManagedServers();

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("MineHub - Available Servers")
                .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("=".repeat(40)).color(NamedTextColor.GRAY));

        if (servers.isEmpty()) {
            player.sendMessage(Component.text("No servers found.", NamedTextColor.GRAY));
        } else {
            for (ServerInfo server : servers) {
                Component line = Component.empty()
                        .append(Component.text(server.isOnline() ? "● " : "× ")
                                .color(server.isOnline() ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .append(Component.text(server.getName(), NamedTextColor.WHITE))
                        .append(Component.text(" - ", NamedTextColor.GRAY))
                        .append(Component.text(
                                server.getDescription().isEmpty() ? "No description" : server.getDescription(),
                                NamedTextColor.GRAY
                        ));
                player.sendMessage(line);
            }
        }

        player.sendMessage(Component.text("Use /minehub connect <server>", NamedTextColor.YELLOW));
        player.sendMessage(Component.empty());
    }

    private void showServerInfo(Player player, String serverName) {
        Optional<ServerInfo> info = serverManagementService.getServerInfo(serverName);

        if (info.isEmpty()) {
            player.sendMessage(Component.text("Server not found.", NamedTextColor.RED));
            return;
        }

        ServerInfo server = info.get();
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("Server Info: " + server.getName())
                .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("Status: " + (server.isOnline() ? "Online" : "Offline"),
                server.isOnline() ? NamedTextColor.GREEN : NamedTextColor.RED));
        player.sendMessage(Component.text("Host: " + server.getAddress(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Owner: " + server.getOwnerName(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Version: " + server.getVersion(), NamedTextColor.GRAY));
        player.sendMessage(Component.text("Players: 0 / " + server.getMaxPlayers(), NamedTextColor.GRAY)); // mock
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("MineHub Commands")
                .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("/minehub list - List servers", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/minehub connect <server> - Join a server", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/minehub info <server> - Show server info", NamedTextColor.YELLOW));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            return CompletableFuture.completedFuture(List.of("list", "info", "connect", "help"));
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("connect") || args[0].equalsIgnoreCase("info"))) {
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
        return true;
    }
}

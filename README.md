# MineHub Velocity Plugin

A dynamic server management plugin for Velocity proxy that allows server administrators to add, remove, and manage Minecraft servers directly through in-game commands.

## Features

- **Dynamic Server Management**: Add and remove servers without restarting the proxy
- **Multi-Version Support**: Compatible with Minecraft versions 1.8.9 - 1.21.8
- **Bedrock Support**: Full compatibility with Geyser for Bedrock Edition players
- **Auto-Cleanup**: Automatically removes servers that have been offline for 72+ hours
- **Database Storage**: Persistent storage using MySQL/MariaDB
- **Permission-Based**: Fine-grained permission control
- **Real-time Status**: Live server status monitoring and updates
- **User-Friendly Commands**: Intuitive command system for easy management

## Requirements

- **Velocity**: 3.4.0 or higher
- **Java**: 17 or higher
- **Database**: MySQL 8.0+ or MariaDB 10.3+
- **Optional**: Geyser plugin for Bedrock support

## Installation

1. Download the plugin JAR file
2. Place it in your Velocity `plugins/` directory
3. Start your Velocity proxy to generate the default configuration
4. Configure your database connection in `plugins/minehub-velocity/config.yml`
5. Restart your Velocity proxy

## Configuration

### Database Setup

1. Create a MySQL/MariaDB database for the plugin:
```sql
CREATE DATABASE minehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'minehub'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON minehub.* TO 'minehub'@'localhost';
FLUSH PRIVILEGES;
```

2. Update the database configuration in `config.yml`:
```yaml
database:
  host: "localhost"
  port: 3306
  database: "minehub"
  username: "minehub"
  password: "your_secure_password"
```

## Commands

### Player Commands
- `/minehub` - Show the server list
- `/minehub list` - Show all available servers
- `/minehub connect <server>` - Connect to a specific server
- `/minehub info <server>` - Show detailed server information
- `/hub` - Alias for `/minehub`

### Server Management Commands
- `/addserver <name> <host> <port> [description]` - Add a new server to the network
- `/removeserver <name>` - Remove a server from the network

## Permissions

### Basic Permissions
- `minehub.use` - Access to basic hub commands (default: true)
- `minehub.connect` - Connect to servers (default: true)

### Management Permissions
- `minehub.addserver` - Add new servers
- `minehub.removeserver` - Remove own servers
- `minehub.removeserver.others` - Remove other players' servers
- `minehub.admin` - Full administrative access

## API Usage

The plugin provides a comprehensive API for other plugins to interact with:

```java
// Get the server management service
ServerManagementService service = plugin.getServerManagementService();

// Add a server programmatically
service.addServer("myserver", "localhost", 25565, playerUuid, playerName)
    .thenAccept(success -> {
        if (success) {
            // Server added successfully
        }
    });

// Get all managed servers
List<ServerInfo> servers = service.getAllManagedServers();

// Check if a server is online
boolean isOnline = service.isServerOnline("myserver");
```

## Version Compatibility

This plugin is designed to work with all Minecraft versions from 1.8.9 to 1.21.8:

- **Legacy Versions**: 1.8.9 - 1.12.2
- **Modern Versions**: 1.13+ - 1.21.8
- **Bedrock Edition**: Via Geyser plugin

## Geyser Integration

The plugin is fully compatible with Geyser, allowing Bedrock Edition players to:
- Connect to Java Edition servers
- Use all plugin features
- Seamlessly switch between servers

## Database Schema

The plugin automatically creates the following tables:

### minehub_servers
- `id` - Auto-incrementing primary key
- `name` - Server name (unique)
- `host` - Server hostname/IP
- `port` - Server port
- `owner_uuid` - Owner's UUID
- `owner_name` - Owner's username
- `created_at` - Creation timestamp
- `last_seen` - Last online timestamp
- `is_online` - Current online status
- `description` - Server description
- `max_players` - Maximum player count
- `version` - Minecraft version

### minehub_players
- `uuid` - Player UUID (primary key)
- `username` - Player username
- `first_join` - First join timestamp
- `last_join` - Last join timestamp
- `last_server` - Last connected server
- `join_count` - Total join count

## Building from Source

1. Clone the repository
2. Ensure you have Java 17+ and Maven installed
3. Run `mvn clean package`
4. The compiled JAR will be in the `target/` directory

## Support

For support, bug reports, or feature requests, please visit our GitHub repository or Discord server.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

We welcome contributions! Please see our contributing guidelines for more information.

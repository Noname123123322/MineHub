# Installation Guide

This guide will walk you through installing and configuring the MineHub Velocity Plugin.

## Prerequisites

Before installing the plugin, make sure you have:

1. **Velocity Proxy**: Version 3.4.0 or higher
2. **Java**: Version 17 or higher
3. **MySQL/MariaDB**: Version 8.0+ (MySQL) or 10.3+ (MariaDB)
4. **Server Access**: Administrative access to your Velocity proxy

## Step 1: Download the Plugin

1. Download the latest version of the plugin JAR file
2. Place it in your Velocity `plugins/` directory

## Step 2: Database Setup

### Create Database and User

Connect to your MySQL/MariaDB server and run:

```sql
-- Create the database
CREATE DATABASE minehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create a user for the plugin
CREATE USER 'minehub'@'localhost' IDENTIFIED BY 'your_secure_password_here';

-- Grant permissions
GRANT ALL PRIVILEGES ON minehub.* TO 'minehub'@'localhost';
FLUSH PRIVILEGES;
```

### Import Schema (Optional)

The plugin will automatically create the required tables, but you can also import the schema manually:

```bash
mysql -u minehub -p minehub < docs/schema.sql
```

## Step 3: Initial Plugin Startup

1. Start your Velocity proxy
2. The plugin will automatically:
   - Create the default configuration file
   - Create database tables
   - Register commands

## Step 4: Configuration

Edit the configuration file at `plugins/minehub-velocity/config.yml`:

```yaml
database:
  host: "your-database-host"
  port: 3306
  database: "minehub"
  username: "minehub"
  password: "your_secure_password_here"

server:
  cleanup-interval-hours: 72
  max-servers-per-user: 5
  default-hub-server: "lobby"
```

## Step 5: Permissions Setup

Configure permissions in your permission plugin:

### For Regular Users:
```yaml
permissions:
  - minehub.use
  - minehub.connect
```

### For Server Owners:
```yaml
permissions:
  - minehub.use
  - minehub.connect
  - minehub.addserver
  - minehub.removeserver
```

### For Administrators:
```yaml
permissions:
  - minehub.*
```

## Step 6: Restart and Test

1. Restart your Velocity proxy
2. Join your server
3. Test the plugin with `/minehub`

## Geyser Integration (Optional)

If you want Bedrock Edition support:

1. Install Geyser on your Velocity proxy
2. Configure Geyser according to their documentation
3. The MineHub plugin will automatically detect and work with Geyser

## Troubleshooting

### Common Issues

**Database Connection Failed**
- Check your database credentials
- Ensure the database server is running
- Verify network connectivity

**Commands Not Working**
- Check if the plugin loaded successfully in the console
- Verify permissions are set correctly
- Look for errors in the logs

**Servers Not Appearing**
- Check if the servers are actually online
- Verify the server addresses are correct
- Check the database for any errors

### Log Files

Check these log files for debugging:
- `logs/latest.log` - Main Velocity log
- Console output during startup

### Getting Help

If you need additional help:
1. Check the plugin logs for error messages
2. Verify your configuration is correct
3. Consult the README.md for additional information
4. Join our Discord server for community support

## Next Steps

Once installed, you can:
1. Add your first server with `/addserver`
2. Configure additional settings in the config file
3. Set up automated backups of your database
4. Monitor server performance and usage

# Changelog

All notable changes to the MineHub Velocity Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-07-24

### Added
- Initial release of MineHub Velocity Plugin
- Dynamic server management system
- Support for Minecraft versions 1.8.9 - 1.21.8
- Full Geyser compatibility for Bedrock Edition players
- MySQL/MariaDB database integration with HikariCP connection pooling
- Automatic server cleanup for offline servers (72+ hours)
- Real-time server status monitoring
- Permission-based command system
- Comprehensive API for other plugins
- Multi-language support framework
- Detailed logging and debugging options

### Features
- `/addserver` command for adding new servers
- `/removeserver` command for removing servers
- `/minehub` command with server listing and connection
- Asynchronous database operations
- Server ping utilities
- Automatic database schema creation
- Configuration management with YAML
- Player tracking and statistics
- Server owner management
- Rate limiting and anti-spam protection

### Security
- SQL injection prevention with prepared statements
- Permission-based access control
- Input validation and sanitization
- Secure database connection handling

### Performance
- Asynchronous operations to prevent blocking
- Connection pooling for database efficiency
- Optimized database queries with proper indexing
- Memory-efficient server status caching
- Configurable cleanup intervals

### Compatibility
- Velocity 3.4.0+ support
- Java 17+ requirement
- MySQL 8.0+ and MariaDB 10.3+ support
- Full Minecraft protocol version compatibility
- Geyser/Floodgate integration
- Cross-platform support (Windows, Linux, macOS)

## [Unreleased]

### Planned Features
- Web dashboard for server management
- Discord bot integration
- Advanced server analytics
- Load balancing capabilities
- Automatic server deployment
- Plugin marketplace integration
- Enhanced GUI support
- Multi-proxy support
- Advanced permission management
- Server templates and presets

### Known Issues
- None currently reported

## Support

For support, bug reports, or feature requests:
- GitHub Issues: [Repository Issues](https://github.com/minehub/velocity-plugin/issues)
- Discord: [MineHub Discord Server](https://discord.gg/minehub)
- Documentation: [Plugin Wiki](https://github.com/minehub/velocity-plugin/wiki)

package t.me.p1azmer.plugin.last_server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import t.me.p1azmer.plugin.last_server.configuration.velocity.YamlConfig;
import t.me.p1azmer.plugin.last_server.dependencies.DatabaseLibrary;

public class Config extends YamlConfig {
    @Ignore
    public static final Config IMP = new Config();

    @Create
    public SETTINGS SETTINGS;
    @Comment("Plugin Settings")
    public static class SETTINGS {
        @Comment("The default server")
        public String DEFAULT_SERVER = "Lobby";
    }
    @Create
    public DATABASE DATABASE;

    @Comment("Database settings")
    public static class DATABASE {

        @Comment("Database type: mariadb, mysql, postgresql, sqlite or h2.")
        public DatabaseLibrary STORAGE_TYPE = DatabaseLibrary.H2;

        @Comment("Settings for Network-based database (like MySQL, PostgreSQL): ")
        public String HOSTNAME = "127.0.0.1:3306";
        public String USER = "root";
        public String PASSWORD = "password";
        public String DATABASE = "minecraft";
        public String CONNECTION_PARAMETERS = "?autoReconnect=true&initialTimeout=1&useSSL=false";
    }
}


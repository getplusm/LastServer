package t.me.p1azmer.plugin.last_server;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import t.me.p1azmer.plugin.last_server.data.SQLRuntimeException;
import t.me.p1azmer.plugin.last_server.data.UserData;
import t.me.p1azmer.plugin.last_server.dependencies.DatabaseLibrary;
import t.me.p1azmer.plugin.last_server.listener.JoinListener;
import t.me.p1azmer.plugin.last_server.listener.integration.LimboAuthListener;

import javax.management.ReflectionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Plugin(
        id = "lastserver",
        name = "LastServer",
        url = "t.me/p1azmer",
        authors = "plazmer",
        description = "Saving last server when player quit and reconnect when join",
        version = BuildConstants.VERSION,
        dependencies = {
                @Dependency(id = "limboauth", optional = true)
        }
)
public class LastServerPlugin {
    private Dao<UserData, String> playerDao;
    @Inject
    private Logger logger;

    private final ProxyServer server;
    private final File dataDirectoryFile;
    private final File configFile;
    private RegisteredServer defaultServer;

    @Inject
    public LastServerPlugin(Logger logger, ProxyServer server, @DataDirectory Path dataDirectory) {
        this.logger = logger;

        this.server = server;

        this.dataDirectoryFile = dataDirectory.toFile();
        this.configFile = new File(this.dataDirectoryFile, "config.yml");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        System.setProperty("com.j256.simplelogging.level", "ERROR");
        try {
            this.reload();
        } catch (SQLRuntimeException | ReflectionException exception) {
            this.getLogger().error("SQL EXCEPTION CAUGHT.", exception);
            this.server.shutdown();
        }
    }

    public void reload() throws ReflectionException {
        Config.IMP.reload(this.configFile, "LastServer");

        this.defaultServer = this.getServer().getServer(Config.IMP.SETTINGS.DEFAULT_SERVER).orElse(null);

        Config.DATABASE dbConfig = Config.IMP.DATABASE;
        DatabaseLibrary databaseLibrary = dbConfig.STORAGE_TYPE;
        ConnectionSource connectionSource;
        try {
            connectionSource = databaseLibrary.connectToORM(
                    this.dataDirectoryFile.toPath().toAbsolutePath(),
                    dbConfig.HOSTNAME,
                    dbConfig.DATABASE + dbConfig.CONNECTION_PARAMETERS,
                    dbConfig.USER,
                    dbConfig.PASSWORD
            );
        } catch (ReflectiveOperationException e) {
            throw new ReflectionException(e);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        try {
            TableUtils.createTableIfNotExists(connectionSource, UserData.class);
            this.playerDao = DaoManager.createDao(connectionSource, UserData.class);

        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }

        EventManager eventManager = this.server.getEventManager();
        eventManager.unregisterListeners(this);
        eventManager.register(this, new JoinListener(this, this.playerDao));
        if (this.server.getPluginManager().getPlugin("limboauth").isPresent())
            eventManager.register(this, new LimboAuthListener(this, this.playerDao));
    }

    public void initialPlayer(@NotNull Player player) {
        UserData basedPlayer = fetchInfo(this.playerDao, player.getUniqueId());
        RegisteredServer registeredServer = this.getDefaultServer();
        if (registeredServer == null) registeredServer = this.getServer().getAllServers().stream()
                .sorted(Comparator.comparing(RegisteredServer::getServerInfo))
                .filter(server -> {
                    AtomicBoolean isActive = new AtomicBoolean(false);
                    server.ping(PingOptions.builder().timeout(5, TimeUnit.SECONDS).build()).thenAccept(serverPing -> {
                        isActive.set(true);
                    }).join();
                    return isActive.get();
                }).findFirst().orElse(null);
        if (registeredServer == null) return;

        ServerConnection server = player.getCurrentServer().orElse(null);
        String serverName = registeredServer.getServerInfo().getName();
        if (server != null) serverName = server.getServerInfo().getName();

        if (basedPlayer == null) {
            basedPlayer = fetchInfo(this.playerDao, player.getUniqueId());


            if (basedPlayer == null) {
                basedPlayer = new UserData(serverName, player.getUniqueId().toString());

                try {
                    this.playerDao.create(basedPlayer);
                } catch (SQLException e) {
                    throw new SQLRuntimeException(e);
                }
            }
        }
    }

    public void updateUserData(@NotNull Player player, @NotNull String server) throws SQLException {
        UUID id = player.getUniqueId();
        UpdateBuilder<UserData, String> updateBuilder = this.playerDao.updateBuilder();
        updateBuilder.where().eq(UserData.COL_UUID, id);
        updateBuilder.updateColumnValue(UserData.COL_LAST_SERVER, server);
        updateBuilder.update();
    }

    public UserData fetchInfo(@NotNull Dao<UserData, String> playerDao, @NotNull UUID uuid) {
        try {
            List<UserData> playerList = playerDao.queryForEq(UserData.COL_UUID, uuid.toString());
            return (playerList != null ? playerList.size() : 0) == 0 ? null : playerList.get(0);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public UserData fetchInfo(@NotNull Dao<UserData, String> playerDao, @NotNull Player player) {
        return this.fetchInfo(playerDao, player.getUniqueId());
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public ProxyServer getServer() {
        return server;
    }

    @Nullable
    public RegisteredServer getDefaultServer() {
        return defaultServer;
    }
}

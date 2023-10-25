package t.me.p1azmer.plugin.last_server.listener;

import com.j256.ormlite.dao.Dao;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import t.me.p1azmer.plugin.last_server.LastServerPlugin;
import t.me.p1azmer.plugin.last_server.data.UserData;

import java.sql.SQLException;

public class JoinListener {

    private final LastServerPlugin plugin;
    private final Dao<UserData, String> playerDao;

    public JoinListener(@NotNull LastServerPlugin plugin, @NotNull Dao<UserData, String> playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }


    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        this.plugin.initialPlayer(event.getPlayer());
    }

    @Subscribe
    public void onChooseServer(PlayerChooseInitialServerEvent event) {
        UserData userData = this.plugin.fetchInfo(this.playerDao, event.getPlayer());
        this.plugin.getServer().getServer(userData.getLastServer()).ifPresent(event::setInitialServer);
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresent(serverConnection ->
        {
            try {
                this.plugin.updateUserData(player, serverConnection.getServerInfo().getName());
            } catch (SQLException ignored) {
            }
        });
    }
}

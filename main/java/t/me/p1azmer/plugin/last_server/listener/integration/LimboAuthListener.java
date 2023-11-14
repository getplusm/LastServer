package t.me.p1azmer.plugin.last_server.listener.integration;

import com.j256.ormlite.dao.Dao;
import com.velocitypowered.api.event.Subscribe;
import net.elytrium.limboauth.event.PostAuthorizationEvent;
import net.elytrium.limboauth.event.TaskEvent;
import org.jetbrains.annotations.NotNull;
import t.me.p1azmer.plugin.last_server.LastServerPlugin;
import t.me.p1azmer.plugin.last_server.data.UserData;

public class LimboAuthListener {
    private final LastServerPlugin plugin;
    private final Dao<UserData, String> playerDao;

    public LimboAuthListener(@NotNull LastServerPlugin plugin, @NotNull Dao<UserData, String> playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }

    @Subscribe
    public void onAuth(PostAuthorizationEvent event) {
        if (event.getResult().equals(TaskEvent.Result.CANCEL)) return;
        this.plugin.getServer().getPlayer(event.getPlayerInfo().getUuid()).ifPresent(player -> {
            UserData userData = this.plugin.fetchInfo(this.playerDao, player);
            plugin.getServer().getServer(userData.getLastServer()).ifPresent(player::createConnectionRequest);
        });
    }
}

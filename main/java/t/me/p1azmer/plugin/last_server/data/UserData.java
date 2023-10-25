package t.me.p1azmer.plugin.last_server.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;

@DatabaseTable(tableName = "LastServer_UserData")
public class UserData {
    public static final String COL_UUID = "UUID";
    public static final String COL_LAST_SERVER = "LAST_SERVER";
    @DatabaseField(columnName = COL_LAST_SERVER)
    private String lastServer;

    @DatabaseField(columnName = COL_UUID)
    private String uuid;

    public UserData() {
    }
    public UserData(@NotNull String lastServer, @NotNull String uuid) {
        this.lastServer = lastServer;
        this.uuid = uuid;
    }

    @NotNull
    public String getLastServer() {
        return lastServer;
    }

    @NotNull
    public String getUuid() {
        return this.uuid;
    }
}

package net.llamadevelopment.PlayerSync.components.provider;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import net.llamadevelopment.PlayerSync.PlayerSync;
import net.llamadevelopment.PlayerSync.components.simplesqlclient.MySqlClient;
import net.llamadevelopment.PlayerSync.components.simplesqlclient.objects.SqlColumn;
import net.llamadevelopment.PlayerSync.components.simplesqlclient.objects.SqlDocument;
import net.llamadevelopment.PlayerSync.components.utils.ItemAPI;
import net.llamadevelopment.PlayerSync.components.utils.SyncPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MySQLProvider extends Provider {

    private MySqlClient client;

    public MySQLProvider(PlayerSync plugin) {
        super(plugin);
    }

    @Override
    public void open(Config config) {
        this.client = new MySqlClient(
                config.getString("mysql.ip"),
                config.getString("mysql.port"),
                config.getString("mysql.username"),
                config.getString("mysql.password"),
                config.getString("mysql.database")
        );

        this.client.createTable("players", "id",
                new SqlColumn("id", SqlColumn.Type.VARCHAR, 64)
                        .append("inventory", SqlColumn.Type.MEDIUMTEXT)
                        .append("enderchest", SqlColumn.Type.MEDIUMTEXT)
                        .append("health", SqlColumn.Type.VARCHAR, 64)
                        .append("food", SqlColumn.Type.INT)
                        .append("level", SqlColumn.Type.INT)
                        .append("exp", SqlColumn.Type.INT)
        );
    }

    @Override
    public void close() {
        this.client.close();
    }

    @Override
    public void getPlayer(Player player, Consumer<SyncPlayer> callback) {
        CompletableFuture.runAsync(() -> {
            SqlDocument result = this.client.find("players", "id", this.getPlugin().getManager().getUserID(player)).first();
            if (result != null) {
                callback.accept(
                        new SyncPlayer(
                                result.getString("inventory"),
                                result.getString("enderchest"),
                                Float.parseFloat(result.getString("health")), // WHY IS THAT A STRING :joy:
                                result.getInt("food"),
                                result.getInt("level"),
                                result.getInt("exp")
                        )
                );
            } else {
                String inv = "empty";
                String ecInv = "empty";

                if (player.getInventory().getContents().size() > 0) {
                    inv = this.getPlugin().getItemAPI().invToString(player.getInventory());
                }

                if (player.getEnderChestInventory().getContents().size() > 0) {
                    ecInv = this.getPlugin().getItemAPI().invToString(player.getEnderChestInventory());
                }

                savePlayer(this.getPlugin().getManager().getUserID(player), inv, ecInv, "20.0", 20, 0, 0);
                callback.accept(new SyncPlayer(inv, ecInv, 20.0f, 20, 0, 0));
            }
        });
    }

    @Override
    public void savePlayer(String uuid, String invString, String ecString, String health, int food, int level, int exp) {
        CompletableFuture.runAsync(() -> {
            SqlDocument result = this.client.find("players", "id", uuid).first();

            if (result != null) {
                this.client.update("players", "id", uuid,
                        new SqlDocument("inventory", invString)
                                .append("enderchest", ecString)
                                .append("health", health)
                                .append("food", food)
                                .append("level", level)
                                .append("exp", exp)
                );
            } else {
                this.client.insert("players",
                        new SqlDocument("id", uuid)
                                .append("inventory", invString)
                                .append("enderchest", ecString)
                                .append("health", health)
                                .append("food", food)
                                .append("level", level)
                                .append("exp", exp)
                );
            }
        });
    }

    @Override
    public String getName() {
        return "mysql";
    }
}

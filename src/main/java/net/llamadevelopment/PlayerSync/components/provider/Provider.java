package net.llamadevelopment.PlayerSync.components.provider;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.llamadevelopment.PlayerSync.PlayerSync;
import net.llamadevelopment.PlayerSync.components.utils.SyncPlayer;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Provider {

    @Getter
    private final PlayerSync plugin;

    public void open(Config c) { }

    public void close() { }

    public void savePlayer(String uuid, String invString, String ecString, String health, int food, int level, int exp) { }

    public void getPlayer(Player player, Consumer<SyncPlayer> callback) {  }

    public String getName() {
        return "undefined";
    }
}

package net.llamadevelopment.PlayerSync.components.utils;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.llamadevelopment.PlayerSync.PlayerSync;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class Manager {

    private final PlayerSync plugin;

    @Getter
    private final ArrayList<String> loaded = new ArrayList<>();
    private final boolean inventory, enderchest, health, food, exp;
    private final int loadDelay;
    private final String idMethod;

    public void savePlayerAsync(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                this.savePlayer(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void savePlayer(Player player) {
        if (!this.loaded.contains(player.getName())) return;
        String inv = "empty";
        String ec = "empty";
        if (enderchest && player.getEnderChestInventory().getContents().size() > 0)
            ec = this.plugin.getItemAPI().invToString(player.getEnderChestInventory());
        if (inventory && player.getInventory().getContents().size() > 0)
            inv = this.plugin.getItemAPI().invToString(player.getInventory());

        this.plugin.getProvider().savePlayer(getUserID(player), inv, ec, player.getHealth() + "", player.getFoodData().getLevel(), player.getExperienceLevel(), player.getExperience());
    }

    public void loadPlayer(Player player) {
        this.loaded.remove(player.getName());

        player.getInventory().clearAll();
        player.getEnderChestInventory().clearAll();
        player.setExperience(0, 0);

        player.sendMessage(Language.get("loadingData"));
        playSound(player, Sound.RANDOM_ORB);

        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, () -> {
            this.plugin.getProvider().getPlayer(player, (syncPlayer -> {

                if (inventory) player.getInventory().setContents(this.plugin.getItemAPI().invFromString(syncPlayer.getInvString()));
                if (enderchest) player.getEnderChestInventory().setContents(this.plugin.getItemAPI().invFromString(syncPlayer.getEcString()));
                if (health) player.setHealth(syncPlayer.getHealth());
                if (food) player.getFoodData().setLevel(syncPlayer.getFood());
                if (exp) player.setExperience(syncPlayer.getExp(), syncPlayer.getLevel());

                loaded.add(player.getName());
                player.sendMessage(Language.get("loadingDone"));
                playSound(player, Sound.RANDOM_LEVELUP);
            }));
        }, this.loadDelay);
    }

    public String getUserID(Player player) {
        switch (idMethod) {
            case "uuid":
                return player.getUniqueId().toString();
            case "name":
                return player.getName();
            default:
                return player.getUniqueId().toString();
        }
    }

    public void playSound(Player player, Sound sound) {
        if (!this.plugin.isSounds()) return;
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.x = (int) player.getPosition().getX();
        packet.y = (int) player.getPosition().getY();
        packet.z = (int) player.getPosition().getZ();
        packet.volume = 1.0f;
        packet.pitch = 1.0f;
        player.dataPacket(packet);
    }

}

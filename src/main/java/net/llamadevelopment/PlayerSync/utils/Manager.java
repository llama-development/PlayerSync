package net.llamadevelopment.PlayerSync.utils;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import net.llamadevelopment.PlayerSync.PlayerSync;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Manager {
    /*
     * _id: uuid
     * inventory: string
     * enderchest: string
     * health: string
     * food: int
     * exp: int/long
     *
     * */
    public static ArrayList<String> loaded = new ArrayList<>();
    public static boolean inventory, enderchest, health, food, exp;
    public static int loadDelay;
    public static String idMethod;

    public static void savePlayerAsync(Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                savePlayer(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void savePlayer(Player player) {
        if (!loaded.contains(player.getName())) return;
        String inv = "empty";
        String ec = "empty";
        if (enderchest && player.getEnderChestInventory().getContents().size() > 0) ec = ItemAPI.invToString(player.getEnderChestInventory());
        if (inventory && player.getInventory().getContents().size() > 0) inv = ItemAPI.invToString(player.getInventory());

        PlayerSync.provider.savePlayer(getUserID(player), inv, ec, player.getHealth() + "", player.getFoodData().getLevel(), player.getExperienceLevel(), player.getExperience());
    }

    public static void loadPlayer(Player player) {
        loaded.remove(player.getName());

        player.getInventory().clearAll();
        player.getEnderChestInventory().clearAll();
        player.setExperience(0, 0);

        player.sendMessage(Language.get("loadingData"));
        playSound(player, Sound.RANDOM_ORB);

        PlayerSync.getInstance().getServer().getScheduler().scheduleDelayedTask(PlayerSync.getInstance(), () -> {
            PlayerSync.provider.getPlayer(player, (syncPlayer -> {
                if (inventory) {
                    String inv = syncPlayer.invString;
                    if (!inv.equalsIgnoreCase("empty")) {
                        String[] itemStrings = inv.split("/");
                        HashMap<Integer, Item> loadedInv = new HashMap<>();
                        for (String str : itemStrings) {
                            ItemWithSlot its = ItemAPI.fromString(str);
                            loadedInv.put(its.slot, its.item);
                        }
                        player.getInventory().setContents(loadedInv);
                    }
                }
                if (enderchest) {
                    String ecInv = syncPlayer.ecString;
                    if (!ecInv.equals("empty")) {
                        String[] ecitemStrings = ecInv.split("/");
                        HashMap<Integer, Item> loadedEcInv = new HashMap<>();
                        for (String str : ecitemStrings) {
                            ItemWithSlot its = ItemAPI.fromString(str);
                            loadedEcInv.put(its.slot, its.item);
                        }
                        player.getEnderChestInventory().setContents(loadedEcInv);
                    }
                }

                if (health) player.setHealth(syncPlayer.health);
                if (food) player.getFoodData().setLevel(syncPlayer.food);
                if (exp) player.setExperience(syncPlayer.exp, syncPlayer.level);

                loaded.add(player.getName());
                player.sendMessage(Language.get("loadingDone"));
                playSound(player, Sound.RANDOM_LEVELUP);
            }));
        }, Manager.loadDelay);
    }

    public static String getUserID(Player player) {
        switch (idMethod) {
            case "uuid":
                return player.getUniqueId().toString();
            case "name":
                return player.getName();
            default:
                return null;
        }
    }

    public static void playSound(Player player, Sound sound) {
        if (!PlayerSync.sounds) return;
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

package net.llamadevelopment.PlayerSync.utils;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

import java.util.Base64;

public class ItemAPI {

    public static String invToString(Inventory inventory) {
        StringBuilder stringInv = new StringBuilder();
        inventory.getContents().forEach(((integer, item) -> stringInv.append(toString(integer, item)).append("/")));
        return stringInv.toString().substring(0, stringInv.toString().length() - 1);
    }

    // itemInfo
    // slot:id:damage:count:CompoundTag(base64)

    public static String toString(int slot, Item item) {
        return slot + ":" + item.getId() + ":" + item.getDamage() + ":" + item.getCount() + ":" +
                (item.hasCompoundTag() ? bytesToBase64(item.getCompoundTag()) : "not");
    }

    public static String bytesToBase64(byte[] src) {
        if (src == null || src.length <= 0) return "not";
        return Base64.getEncoder().encodeToString(src);
    }

    // itemInfo
    // slot:id:damage:count:CompoundTag(base64)

    public static ItemWithSlot fromString(String itemString) throws NumberFormatException {
        String[] info = itemString.split(":");
        int slot = Integer.parseInt(info[0]);
        Item item = Item.get(
                Integer.parseInt(info[1]),
                Integer.parseInt(info[2]),
                Integer.parseInt(info[3])
        );
        if (!info[4].equals("not")) item.setCompoundTag(base64ToBytes(info[4]));
        return new ItemWithSlot(slot, item);
    }

    public static byte[] base64ToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) return null;
        return Base64.getDecoder().decode(hexString);
    }

}

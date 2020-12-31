package net.llamadevelopment.PlayerSync.components.utils;

import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ItemAPI {

    public String invToString(Inventory inventory) {
        StringBuilder stringInv = new StringBuilder();
        inventory.getContents().forEach(((integer, item) -> stringInv.append(itemToString(integer, item)).append(";")));
        return stringInv.substring(0, stringInv.toString().length() - 1);
    }

    public Map<Integer, Item> invFromString(String string) {
        if (!string.equalsIgnoreCase("empty")) {
            String[] itemStrings = string.split(";");
            final Map<Integer, Item> loadedInv = new HashMap<>();
            for (String str : itemStrings) {
                ItemWithSlot its = itemFromString(str);
                loadedInv.put(its.getSlot(), its.getItem());
            }
            return loadedInv;
        } else return new HashMap<>();
    }

    // itemInfo
    // slot:id:damage:count:CompoundTag(base64)

    public String itemToString(int slot, Item item) {
        return slot + ":" + item.getId() + ":" + item.getDamage() + ":" + item.getCount() + ":" +
                (item.hasCompoundTag() ? bytesToBase64(item.getCompoundTag()) : "not");
    }

    private String bytesToBase64(byte[] src) {
        if (src == null || src.length <= 0) return "not";
        return Base64.getEncoder().encodeToString(src);
    }

    // itemInfo
    // slot:id:damage:count:CompoundTag(base64)

    public ItemWithSlot itemFromString(String itemString) throws NumberFormatException {
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

    private byte[] base64ToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) return null;
        return Base64.getDecoder().decode(hexString);
    }

}

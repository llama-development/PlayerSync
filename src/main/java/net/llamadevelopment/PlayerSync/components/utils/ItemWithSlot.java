package net.llamadevelopment.PlayerSync.components.utils;

import cn.nukkit.item.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ItemWithSlot {

    private final Integer slot;
    private final Item item;

}

package net.llamadevelopment.PlayerSync.components.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SyncPlayer {

    private final String invString, ecString;
    private final float health;
    private final int food, exp, level;

}

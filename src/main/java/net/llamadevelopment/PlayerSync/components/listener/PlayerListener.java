package net.llamadevelopment.PlayerSync.components.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import lombok.RequiredArgsConstructor;
import net.llamadevelopment.PlayerSync.PlayerSync;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final PlayerSync plugin;

    @EventHandler
    public void on(PlayerJoinEvent event) {
        this.plugin.getManager().loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        if (!this.plugin.getManager().getLoaded().contains(event.getPlayer().getName())) event.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        this.plugin.getManager().savePlayerAsync(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerMoveEvent event) {
        if (!this.plugin.getManager().getLoaded().contains(event.getPlayer().getName())) event.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (!event.isCancelled() && !this.plugin.getManager().getLoaded().contains(event.getPlayer().getName())) event.setCancelled(true);
    }

}

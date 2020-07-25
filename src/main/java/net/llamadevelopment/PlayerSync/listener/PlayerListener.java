package net.llamadevelopment.PlayerSync.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.*;
import net.llamadevelopment.PlayerSync.utils.Manager;

public class PlayerListener implements Listener {

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Manager.loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        if (!Manager.loaded.contains(event.getPlayer().getName())) event.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Manager.savePlayerAsync(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerMoveEvent event) {
        if (!Manager.loaded.contains(event.getPlayer().getName())) event.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        if (!event.isCancelled() && !Manager.loaded.contains(event.getPlayer().getName())) event.setCancelled(true);
    }

}

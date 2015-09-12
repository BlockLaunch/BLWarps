package com.blocklaunch.blwarps.eventhandlers;

import com.blocklaunch.blwarps.BLWarps;
import com.google.common.base.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.HealEntityEvent;

public class PlayerChangeHealthEventHandler {

    private BLWarps plugin;

    public PlayerChangeHealthEventHandler(BLWarps plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void playerChangeHealth(HealEntityEvent event) {
        if (this.plugin.getConfig().isPvpProtect()) {
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()) {
             // pvp-protect setting is enabled
                if (this.plugin.getWarpManager().isWarping(player.get())) {
                    // Player is warping
                    if (event.getFinalHealAmount() < event.getOriginalHealAmount()) {
                        // Player was damaged
                        this.plugin.getWarpManager().cancelWarp(player.get());
                    }
                }
            }
        }
    }
}

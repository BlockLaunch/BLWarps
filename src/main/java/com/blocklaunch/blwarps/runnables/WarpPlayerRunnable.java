package com.blocklaunch.blwarps.runnables;

import org.spongepowered.api.entity.living.player.Player;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Constants;
import com.blocklaunch.blwarps.Warp;

public class WarpPlayerRunnable implements Runnable {

    private BLWarps plugin;
    private Player player;
    private Warp warp;

    public WarpPlayerRunnable(BLWarps plugin, Player player, Warp warp) {
        this.plugin = plugin;
        this.player = player;
        this.warp = warp;
    }

    @Override
    public void run() {
        if (!this.player.transferToWorld(this.warp.getWorld(), this.warp.getPosition())) {
            this.player.sendMessage(Constants.WORLD_NOT_FOUND_MSG);
        }

        this.plugin.getWarpManager().warpCompleted(this.player);

    }

    public Warp getWarp() {
        return this.warp;
    }

}

package com.blocklaunch.blwarps.eventhandlers;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Warp;
import com.blocklaunch.blwarps.exceptions.MultipleWarpRegionsException;
import com.blocklaunch.blwarps.region.WarpRegion;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;

import org.khelekore.prtree.MBR;
import org.khelekore.prtree.SimpleMBR;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import java.util.ArrayList;
import java.util.List;

public class PlayerMoveEventHandler {

    private BLWarps plugin;

    public PlayerMoveEventHandler(BLWarps plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void playerMove(DisplaceEntityEvent.TargetPlayer event) throws Exception {
        if (event.getFromTransform().equals(event.getToTransform())) {
            // Don't do anything if a player just looks around, but doesn't move
            return;
        }

        Player player = event.getTargetEntity();
        Vector3d location = event.getToTransform().getPosition();

        // If pvp-protect config setting is on, cancel the warp
        if (this.plugin.getConfig().isPvpProtect()) {
            if (this.plugin.getWarpManager().isWarping(player)) {
                this.plugin.getWarpManager().cancelWarp(player);
            }
        }

        List<WarpRegion> warpRegions = getContainingRegions(location);

        if (warpRegions.isEmpty()) {
            return;
        }

        if (warpRegions.size() != 1) {
            // There should only ever be 1 WarpRegion in the list - if not,
            // there are more than one warp regions occupying the same space
            throw new MultipleWarpRegionsException(location);
        }

        WarpRegion region = warpRegions.get(0);
        Optional<Warp> linkedWarpOpt = this.plugin.getWarpManager().getOne(region.getLinkedWarpName());

        if (!linkedWarpOpt.isPresent()) {
            this.plugin.getLogger().warn(
                    "Player " + player.getName() + " attempted to use warp region " + region.getName() + ", but the linked warp "
                            + region.getLinkedWarpName() + " was not found!");

            return;
        }

        Optional<Warp> existingDestinationOpt = this.plugin.getWarpManager().getPlayerDestination(player);
        // Cancel the possibly existing scheduled warp before warping to the new
        // warp
        if (existingDestinationOpt.isPresent()) {
            // Only cancel it if the existing scheduled warp is different than
            // the new one
            if (!existingDestinationOpt.get().getName().equals(linkedWarpOpt.get().getName())) {
                this.plugin.getWarpManager().cancelWarp(player);
                this.plugin.getWarpManager().scheduleWarp(player, linkedWarpOpt.get());
            }
        } else {
            this.plugin.getWarpManager().scheduleWarp(player, linkedWarpOpt.get());
        }

    }

    private List<WarpRegion> getContainingRegions(Vector3d location) {
        MBR locationMBR = new SimpleMBR(location. getX(), location.getX(), location.getY(), location.getY(), location.getZ(), location.getZ());
        List<WarpRegion> warpRegions = new ArrayList<WarpRegion>();

        // Find intersecting regions & store in list
        this.plugin.getWarpRegionManager().getPRTree().find(locationMBR, warpRegions);

        return warpRegions;
    }

}

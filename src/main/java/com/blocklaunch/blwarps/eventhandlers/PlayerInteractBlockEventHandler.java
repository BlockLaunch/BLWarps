package com.blocklaunch.blwarps.eventhandlers;

import com.blocklaunch.blwarps.BLWarps;
import com.google.common.base.Optional;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.manipulator.tileentity.SignData;
import org.spongepowered.api.entity.EntityInteractionTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.Location;

import java.util.List;

/**
 * Listens for Players interacting with blocks If
 */
public class PlayerInteractBlockEventHandler {

    private BLWarps plugin;

    public PlayerInteractBlockEventHandler(BLWarps plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void playerInteractBlock(PlayerInteractBlockEvent event) {
        // Ensure right click
        if (!(event.getInteractionType() == EntityInteractionTypes.USE)) {
            return;
        }

        Location block = event.getBlock();

        // Ensure the block is a tile entity
        if (!block.getTileEntity().isPresent()) {
            return;
        }
        // Ensure the tile entity is a sign
        if (!(block.getTileEntity().get().getType() == TileEntityTypes.SIGN)) {
            return;
        }

        TileEntity signEntity = block.getTileEntity().get();
        // Get the SignData
        Optional<SignData> signData = signEntity.getOrCreate(SignData.class);
        // Ensure the sign actually has text on it
        if (!signData.isPresent()) {
            return;
        }
        // Validate that this is supposed to be a warp sign
        List<Text> lines = signData.get().getLines();
        if (!Texts.toPlain(lines.get(1)).equalsIgnoreCase(SignChangeEventHandler.WARP_SIGN_PREFIX)) {
            return;
        }
        // Don't need to validate that the warp actually exists --> Command
        // executor will take care
        // of it. (along with permissions)
        String command = "warp '" + Texts.toPlain(lines.get(2)) + "'";
        this.plugin.getGame().getCommandDispatcher().process(event.getUser(), command);

    }

}

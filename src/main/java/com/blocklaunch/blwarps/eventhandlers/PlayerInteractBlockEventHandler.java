package com.blocklaunch.blwarps.eventhandlers;

import org.spongepowered.api.world.World;

import com.blocklaunch.blwarps.BLWarps;
import com.google.common.base.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;

/**
 * Listens for Players interacting with blocks If
 */
public class PlayerInteractBlockEventHandler {

    private BLWarps plugin;

    public PlayerInteractBlockEventHandler(BLWarps plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void playerInteractBlock(InteractBlockEvent.Use event) {
        BlockSnapshot blockSnapshot = event.getTargetBlock();
        Optional<Location<World>> block = blockSnapshot.getLocation();
        Optional<Player> player = event.getCause().first(Player.class);

        // Ensure the player used a block
        if (!player.isPresent()) {
            return;
        }

        // Ensure the block is a tile entity
        if (!block.get().getTileEntity().isPresent()) {
            return;
        }

        // Ensure the tile entity is a sign
        if (!(block.get().getTileEntity().get().getType() == TileEntityTypes.SIGN)) {
            return;
        }

        TileEntity signEntity = block.get().getTileEntity().get();
        // Get the SignData
        Optional<SignData> signData = signEntity.getOrCreate(SignData.class);
        // Ensure the sign actually has text on it
        if (!signData.isPresent()) {
            return;
        }
        // Validate that this is supposed to be a warp sign
        ListValue<Text> lines = signData.get().lines();
        if (!Texts.toPlain(lines.get(1)).equalsIgnoreCase(SignChangeEventHandler.WARP_SIGN_PREFIX)) {
            return;
        }
        // Don't need to validate that the warp actually exists --> Command
        // executor will take care
        // of it. (along with permissions)
        String command = "warp " + Texts.toPlain(lines.get(2));
        this.plugin.getGame().getCommandDispatcher().process((CommandSource) player, command);

    }

}

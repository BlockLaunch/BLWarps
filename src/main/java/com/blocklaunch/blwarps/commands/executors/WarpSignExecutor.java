package com.blocklaunch.blwarps.commands.executors;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Constants;
import com.blocklaunch.blwarps.Warp;
import com.google.common.base.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class WarpSignExecutor implements CommandExecutor {

    private BLWarps plugin;

    public WarpSignExecutor(BLWarps plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Constants.MUST_BE_PLAYER_MSG);
            return CommandResult.empty();
        }
        Player player = (Player) source;

        Optional<Warp> optWarp = args.getOne("warp");
        if (!optWarp.isPresent()) {
            source.sendMessage(Constants.WARP_NOT_FOUND_MSG);
            return CommandResult.empty();
        }

        Warp warp = optWarp.get();

        if (this.plugin.getUtil().hasPermission(player, warp) == false) {
            player.sendMessage(Constants.NO_PERMISSION_MSG);
            return CommandResult.empty();
        }

        ItemStack warpSign =
                this.plugin.getGame().getRegistry().createItemBuilder().itemType(ItemTypes.SIGN).quantity(1)
                        .itemData(this.plugin.getUtil().generateWarpSignData(warp)).build();

        if (player.getInventory().offer(warpSign) == false) {
            player.sendMessage(Constants.INVENTORY_FULL_MSG);
            return CommandResult.empty();
        }

        return CommandResult.success();
    }

}

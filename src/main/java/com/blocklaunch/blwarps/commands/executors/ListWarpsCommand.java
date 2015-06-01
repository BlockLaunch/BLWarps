package com.blocklaunch.blwarps.commands.executors;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Warp;
import com.blocklaunch.blwarps.managers.WarpManager;

public class ListWarpsCommand implements CommandExecutor {
	
    private static final Text NO_WARPS_MSG = Texts.of(TextColors.GREEN, BLWarps.PREFIX + " There were no warps to display.");
    private static final Text LIST_TITLE = Texts.of(TextColors.BLUE, "Warps");

	private BLWarps plugin;
	
	public ListWarpsCommand(BLWarps plugin) {
		this.plugin = plugin;
	}

    /**
     * Gets the currently loaded warps, paginates them into pages of size WARPS_PER_PAGE, and sends
     * the warp names in a message to the player
     * 
     * @param source
     * @param args
     * @return
     * @throws CommandException
     */
    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {

        if (WarpManager.warps.isEmpty()) {
            source.sendMessage(NO_WARPS_MSG);
            return CommandResult.success();
        }
        
        List<Text> warpNames = new ArrayList<Text>();
        for(Warp w : WarpManager.warps) {
        	warpNames.add(Texts.of(TextColors.GOLD, w.getName()));
        }
        
        PaginationService paginationService = plugin.getGame().getServiceManager().provide(PaginationService.class).get();
        paginationService.builder().contents(warpNames).title(LIST_TITLE).sendTo(source);

        return CommandResult.success();
    }

}

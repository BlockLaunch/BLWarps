package com.blocklaunch.blwarps.eventhandlers;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Util;
import com.blocklaunch.blwarps.Warp;
import com.google.common.base.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.MessageSinkEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandSource;

/**
 * Class to listen for chat messages being sent If there are any words in the
 * text that are the name of a warp, replace that word with a Text object which,
 * when clicked on, will warp the player to that warp. If the player does not
 * have permission to use that warp, the text will not be transformed
 */
public class PlayerChatEventHandler {

    private BLWarps plugin;

    public PlayerChatEventHandler(BLWarps plugin) {
        this.plugin = plugin;
    }

    private static final String SPACE = " ";

    @Listener
    public void playerChatEvent(MessageSinkEvent event) {
        Text originalMessage = event.getMessage();
        String originalMessagePlain = Texts.toPlain(originalMessage);
        String[] originalMessageWords = originalMessagePlain.split(SPACE);
        Optional<Player> playerOptional = event.getCause().first(Player.class);

        for (String word : originalMessageWords) {
            if (this.plugin.getWarpManager().getNames().contains(word.toLowerCase())) {
                Optional<Warp> optWarp = this.plugin.getWarpManager().getOne(word);
                if (!optWarp.isPresent()) {
                    continue;
                }

                Warp warp = optWarp.get();
                if (!playerOptional.isPresent()) {
                    continue;
                }

                CommandSource source = playerOptional.get();
                if (!this.plugin.getUtil().hasPermission(source, warp)) {
                    continue;
                }

                this.plugin.getUtil();
                Text text = Util.warpText(warp);
                // TODO Replace the original text's word representing the warp
                // with this one ^
            }
        }

    }

}

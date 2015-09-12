package com.blocklaunch.blwarps;

import com.blocklaunch.blwarps.region.WarpRegion;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandSource;

public class Util {

    private BLWarps plugin;
    public static final String WARP_SIGN_PREFIX = "[Warp]";

    public Util(BLWarps plugin) {
        this.plugin = plugin;
    }

    //
    // Warp Text
    //

    public static Text warpText(Warp warp) {
        return Texts.builder(warp.getName()).color(TextColors.GOLD).onClick(TextActions.runCommand("/warp " + warp.getName()))
                .onHover(TextActions.showText(Texts.of("Warp to ", TextColors.GOLD, warp.getName()))).build();

    }

    public static Text deleteWarpText(Warp warp) {
        return Texts.builder("[X]").color(TextColors.RED).onClick(TextActions.runCommand("/warp delete " + warp.getName()))
                .onHover(TextActions.showText(Texts.of(TextColors.RED, "Delete ", TextColors.GOLD, warp.getName()))).build();
    }

    public static Text undoDeleteWarpText(Warp warp) {
        return Texts.builder("Undo").color(TextColors.RED)
                .onClick(TextActions.runCommand("/warp set " + warp.getName() + " " + Util.vector3dToCommandFriendlyString(warp.getPosition())))
                .onHover(TextActions.showText(Texts.of(TextColors.RED, "Undo delete warp ", TextColors.GOLD, warp.getName()))).build();
    }

    //
    // Warp Group Text
    //

    public static Text warpGroupInfoText(String groupName) {
        return Texts.builder(groupName).color(TextColors.GOLD).onClick(TextActions.runCommand("/warp group info " + groupName))
                .onHover(TextActions.showText(Texts.of("Show ", TextColors.GOLD, groupName, TextColors.WHITE, " info."))).build();
    }

    //
    // Warp Region Text
    //

    public static Text warpRegionInfoText(WarpRegion region) {
        return Texts.builder(region.getName()).color(TextColors.GOLD).onClick(TextActions.runCommand("/warp region info " + region.getName()))
                .onHover(TextActions.showText(Texts.of("Show ", TextColors.GOLD, region.getName(), TextColors.WHITE, " info."))).build();
    }

    public static Text deleteWarpRegionText(WarpRegion region) {
        return Texts.builder("[X]").color(TextColors.RED).onClick(TextActions.runCommand("/warp region delete " + region.getName()))
                .onHover(TextActions.showText(Texts.of(TextColors.RED, "Delete warp region ", TextColors.GOLD, region.getName()))).build();
    }

    public static Text undoDeleteWarpRegionText(WarpRegion region) {
        return Texts
                .builder("Undo")
                .color(TextColors.RED)
                .onClick(
                        TextActions.runCommand("/warp region add " + region.getName() + " " + region.getLinkedWarpName() + " "
                                + Util.vector3dToCommandFriendlyString(region.getMinLoc())
                                + " " + Util.vector3dToCommandFriendlyString(region.getMaxLoc())))
                .onHover(TextActions.showText(Texts.of(TextColors.RED, "Undo delete warp region ", TextColors.GOLD, region.getName()))).build();
    }

    public static String vector3dToCommandFriendlyString(Vector3d vector) {
        StringBuilder sb = new StringBuilder();
        sb.append(vector.getX());
        sb.append(",");
        sb.append(vector.getY());
        sb.append(",");
        sb.append(vector.getZ());

        return sb.toString();
    }

    public boolean hasPermission(CommandSource source, Warp warp) {
        String warpPermission = "blwarps.warp." + warp.getName();
        String groupPermissionBase = "blwarps.warp.group.";
        String wildCardPermission = "blwarps.warp.*";

        boolean playerIsValid = false;

        // Check permission for individual warp or wildcard
        if (source.hasPermission(warpPermission) || source.hasPermission(wildCardPermission)) {
            playerIsValid = true;
        }

        // Check permission for warp groups
        for (String groupName : warp.getGroups()) {
            String permission = groupPermissionBase + groupName;
            if (source.hasPermission(permission)) {
                playerIsValid = true;
            }
        }

        return playerIsValid;
    }

    /**
     * @param warp the warp to represent on the SignData
     * @return the formatted SignData indicating a valid warp sign
     */
    public SignData generateWarpSignData(Warp warp) {
        Optional<DataManipulatorBuilder<SignData, ImmutableSignData>> builder =
                this.plugin.getGame().getRegistry().getManipulatorRegistry().getBuilder(SignData.class);
        SignData signData = builder.get().create();
        ListValue<Text> lines = signData.lines();

        lines.set(0, Texts.of());
        lines.set(1, Texts.of(TextColors.DARK_BLUE, WARP_SIGN_PREFIX));
        lines.set(2, Texts.of(TextColors.GOLD, warp.getName()));
        lines.set(3, Texts.of());
        
        signData.set(lines);
        
        return signData;

    }
}

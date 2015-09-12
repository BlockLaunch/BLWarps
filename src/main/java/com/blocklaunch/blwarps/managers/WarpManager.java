package com.blocklaunch.blwarps.managers;

import com.blocklaunch.blwarps.BLWarps;
import com.blocklaunch.blwarps.Constants;
import com.blocklaunch.blwarps.Util;
import com.blocklaunch.blwarps.Warp;
import com.blocklaunch.blwarps.managers.storage.StorageManager;
import com.blocklaunch.blwarps.runnables.WarpPlayerRunnable;
import com.google.common.base.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

public class WarpManager extends WarpBaseManager<Warp> {

    private Map<Player, Task> warpsInProgress = new HashMap<Player, Task>();

    private Validator validator;

    public WarpManager(BLWarps plugin, StorageManager<Warp> storage) {
        super(plugin, storage);
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Adds a warp with the passed in name and location
     *
     * @param warpName The name of the warp
     * @param warpLocation The location of the warp
     * @return An error if the warp already exists, Optional.absent() otherwise
     */
    @Override
    public Optional<String> addNew(Warp newWarp) {

        for (Warp warp : this.payload) {
            if (warp.getName().equalsIgnoreCase(newWarp.getName())) {
                // A warp with that name already exists
                return Optional.of(Constants.WARP_NAME_EXISTS);
            }
            if (warp.locationIsSame(newWarp)) {
                return Optional.of(Constants.WARP_LOCATION_EXISTS);
            }
        }
        Set<ConstraintViolation<Warp>> violations = this.validator.validate(newWarp);
        if (!violations.isEmpty()) {
            return Optional.of(violations.iterator().next().getMessage());
        }

        this.payload.add(newWarp);
        this.names.add(newWarp.getName());

        // Save warps after putting a new one in rather than saving when server
        // shuts down to prevent loss of data if the server crashed
        this.storage.saveNew(newWarp);

        // No errors, return an absent optional
        return Optional.absent();

    }

    /**
     * Adds the warp to the specified group (puts the group's name in the list
     * of the warp's groups)
     *
     * @param warp The warp to be added to the group
     * @param group The group to add the warp to
     */
    public void addWarpToGroup(Warp warp, String group) {
        if (warp.getGroups().contains(group)) {
            return;
        }
        warp.getGroups().add(group);
        this.storage.update(warp);
    }

    /**
     * Removes the warp from the specified group (removes the group's name from
     * the list of the warp's groups)
     *
     * @param warp The warp to be removed from the group
     * @param group The group to remove the warp from
     */
    public void removeWarpFromGroup(Warp warp, String group) {
        if (!warp.getGroups().contains(group)) {
            return;
        }
        warp.getGroups().remove(group);
        this.storage.update(warp);
    }

    /**
     * Schedules a task to warp the specified player to the specified warp
     *
     * @param player The player to be warped
     * @param warp The location the player is to be warped to
     * @return An error if one exists, or Optional.absent() otherwise
     */
    public void scheduleWarp(Player player, Warp warp) {
        // Schedule the task
        Task scheduledWarpTask =
                this.plugin.getGame().getScheduler().createTaskBuilder().delay(this.plugin.getConfig().getWarpDelay(), TimeUnit.SECONDS)
                        .execute(new WarpPlayerRunnable(this.plugin, player, warp)).submit(this.plugin);

        this.warpsInProgress.put(player, scheduledWarpTask);

        // Notify the player that they will be warped
        player.sendMessage(Texts.of(TextColors.GREEN, Constants.PREFIX + " You will be warped to ", Util.warpText(warp), " in ",
                TextColors.GOLD, this.plugin.getConfig().getWarpDelay(), TextColors.GREEN, " seconds."));

        // If the pvp-protect config setting is set to true, warn the player not
        // to move
        if (this.plugin.getConfig().isPvpProtect() == true) {
            player.sendMessage(Constants.DONT_MOVE_MSG);
        }

    }

    public void cancelWarp(Player player) {
        Task task = this.warpsInProgress.get(player);
        task.cancel();
        Warp previousDestination = ((WarpPlayerRunnable) task.getRunnable()).getWarp();

        player.sendMessage(Texts.of(TextColors.RED, Constants.PREFIX + " Your warp to ", Util.warpText(previousDestination),
                " has been canceled!"));
        this.warpsInProgress.remove(player);
    }

    public Optional<Warp> getPlayerDestination(Player player) {
        if (!this.warpsInProgress.containsKey(player)) {
            return Optional.absent();
        }
        Task task = this.warpsInProgress.get(player);
        Warp destination = ((WarpPlayerRunnable) task.getRunnable()).getWarp();
        return Optional.of(destination);
    }

    /**
     * Removes the specified player from the map of players who are waiting to
     * be warped
     *
     * @param player The player that has been teleported
     */
    public void warpCompleted(Player player) {
        this.warpsInProgress.remove(player);
    }

    /**
     * Checks if the passed in player currently has a warp request
     *
     * @param player The player to check
     * @return true if they are about to be warped, false if not.
     */
    public boolean isWarping(Player player) {
        return this.warpsInProgress.containsKey(player);
    }

}

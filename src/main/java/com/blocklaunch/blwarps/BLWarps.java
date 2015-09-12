package com.blocklaunch.blwarps;

import com.blocklaunch.blwarps.commands.WarpCommandGenerator;
import com.blocklaunch.blwarps.eventhandlers.PlayerChangeHealthEventHandler;
import com.blocklaunch.blwarps.eventhandlers.PlayerChatEventHandler;
import com.blocklaunch.blwarps.eventhandlers.PlayerInteractBlockEventHandler;
import com.blocklaunch.blwarps.eventhandlers.PlayerMoveEventHandler;
import com.blocklaunch.blwarps.eventhandlers.SignChangeEventHandler;
import com.blocklaunch.blwarps.managers.WarpBaseManager;
import com.blocklaunch.blwarps.managers.WarpManager;
import com.blocklaunch.blwarps.managers.WarpRegionManager;
import com.blocklaunch.blwarps.managers.storage.FlatFileManager;
import com.blocklaunch.blwarps.managers.storage.RestManager;
import com.blocklaunch.blwarps.managers.storage.StorageManager;
import com.blocklaunch.blwarps.managers.storage.sql.warp.SqlWarpManager;
import com.blocklaunch.blwarps.managers.storage.sql.warpregion.SqlWarpRegionManager;
import com.blocklaunch.blwarps.region.WarpRegion;
import com.google.inject.Inject;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.service.event.EventManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class BLWarps {

    private BLWarpsConfiguration config;

    private Util util = new Util(this);

    private Map<Class<? extends WarpBase>, WarpBaseManager<? extends WarpBase>> warpBaseManagers =
            new HashMap<Class<? extends WarpBase>, WarpBaseManager<? extends WarpBase>>();

    @Inject private Game game;

    @Inject private Logger logger;

    @Inject @DefaultConfig(sharedRoot = false) private File configFile;

    @Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        setupConfig();
        setupManagers();
        registerCommands();
        registerEventHandlers();
    }

    private void registerCommands() {
        this.logger.info("Registering commands");
        this.game.getCommandDispatcher().register(this, new WarpCommandGenerator(this).mainWarpCommand(), "warp");
    }

    private void setupConfig() {
        if (!this.configFile.exists()) {
            saveDefaultConfig();
        } else {
            loadConfig();
        }
    }

    /**
     * Reads in config values supplied from the ConfigManager. Falls back on the
     * default configuration values in Settings
     */
    private void loadConfig() {
        ConfigurationNode rawConfig = null;
        try {
            rawConfig = this.configLoader.load();
            this.config = BLWarpsConfiguration.MAPPER.bindToNew().populate(rawConfig);
        } catch (IOException e) {
            this.logger.warn("The configuration could not be loaded! Using the default configuration");
        } catch (IllegalArgumentException e) {
            // Everything after this is only for stringifying the array of all
            // StorageType values
            StringBuilder sb = new StringBuilder();
            StorageType[] storageTypes = StorageType.values();
            for (int i = 0; i < storageTypes.length; i++) {
                sb.append(storageTypes[i].toString());
                if (i + 1 != storageTypes.length) {
                    sb.append(", ");
                }
            }
            this.logger.warn("The specified storage type could not be found. Reverting to flatfile storage. Try: " + sb.toString());
        } catch (ObjectMappingException e) {
            this.logger.warn("There was an loading the configuration." + e.getStackTrace());
        }
    }

    /**
     * Saves a config file with default values if it does not already exist
     *
     * @return true if default config was successfully created, false if the
     *         file was not created
     */
    private void saveDefaultConfig() {
        try {
            this.logger.info("Generating config file...");
            this.configFile.getParentFile().mkdirs();
            this.configFile.createNewFile();
            CommentedConfigurationNode rawConfig = this.configLoader.load();

            try {
                // Populate config with default values
                this.config = BLWarpsConfiguration.MAPPER.bindToNew().populate(rawConfig);
                BLWarpsConfiguration.MAPPER.bind(this.config).serialize(rawConfig);
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }

            this.configLoader.save(rawConfig);
            this.logger.info("Config file successfully generated.");
        } catch (IOException exception) {
            this.logger.warn("The default configuration could not be created!");
        }
    }

    private void setupManagers() {
        File warpsFile = new File(this.configFile.getParentFile(), "warps.json");
        File warpRegionFile = new File(this.configFile.getParentFile(), "warp-regions.json");

        StorageManager<Warp> warpStorage;
        StorageManager<WarpRegion> warpRegionStorage;

        switch (this.config.getStorageType()) {
            case FLATFILE:
                warpStorage = new FlatFileManager<Warp>(Warp.class, this, warpsFile);
                warpRegionStorage = new FlatFileManager<WarpRegion>(WarpRegion.class, this, warpRegionFile);
                break;
            case REST:
                warpStorage = new RestManager<Warp>(Warp.class, this);
                warpRegionStorage = new RestManager<WarpRegion>(WarpRegion.class, this);
                break;
            case SQL:
                warpStorage = new SqlWarpManager(this);
                warpRegionStorage = new SqlWarpRegionManager(this);
                break;
            default:
                warpStorage = new FlatFileManager<Warp>(Warp.class, this, warpsFile);
                warpRegionStorage = new FlatFileManager<WarpRegion>(WarpRegion.class, this, warpRegionFile);
                break;
        }

        this.warpBaseManagers.put(Warp.class, new WarpManager(this, warpStorage));
        this.warpBaseManagers.put(WarpRegion.class, new WarpRegionManager(this, warpRegionStorage));

        for (WarpBaseManager<? extends WarpBase> manager : this.warpBaseManagers.values()) {
            manager.load();
        }

    }

    private void registerEventHandlers() {
        EventManager eventManager = this.game.getEventManager();
        // Filter chat & replace warp names in chat w/ clickable links
        eventManager.registerListeners(this, new PlayerChatEventHandler(this));
        // Watch for players right-clicking warp signs
        eventManager.registerListeners(this, new PlayerInteractBlockEventHandler(this));
        // Watch for warp signs being created
        eventManager.registerListeners(this, new SignChangeEventHandler(this));
        // Watch for player movement (warp regions, cancelling warps)
        eventManager.registerListeners(this, new PlayerMoveEventHandler(this));
        // Watch for player damage - cancel warp if pvp-protect setting is
        // enabled
        eventManager.registerListeners(this, new PlayerChangeHealthEventHandler(this));
    }

    public Logger getLogger() {
        return this.logger;
    }

    public WarpManager getWarpManager() {
        return (WarpManager) this.warpBaseManagers.get(Warp.class);
    }

    public WarpRegionManager getWarpRegionManager() {
        return (WarpRegionManager) this.warpBaseManagers.get(WarpRegion.class);
    }

    public BLWarpsConfiguration getConfig() {
        return this.config;
    }

    public Game getGame() {
        return this.game;
    }

    public Util getUtil() {
        return this.util;
    }

}

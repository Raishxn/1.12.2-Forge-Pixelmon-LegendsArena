package com.raishxn.legendsarena;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.raishxn.legendsarena.capabilities.CapabilityRegistry;
import com.raishxn.legendsarena.commands.CommandRanked;
import com.raishxn.legendsarena.config.ConfigManager;
import com.raishxn.legendsarena.config.DatabaseManager;
import com.raishxn.legendsarena.events.EventHandler;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.raishxn.legendsarena.commands.CommandRanked;
import com.raishxn.legendsarena.commands.CommandRankedAdmin; // Importar o novo comando
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class LegendsArena {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static EventHandler eventHandler;
    private static ConfigManager configManager;
    private static DatabaseManager databaseManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);

        configManager = new ConfigManager(event.getModConfigurationDirectory());
        configManager.initialize();

        // CORREÇÃO: Passamos o caminho da config para o DatabaseManager
        databaseManager = new DatabaseManager(event.getModConfigurationDirectory());
        databaseManager.initialize();

        PacketHandler.registerMessages();
        CapabilityRegistry.register();

        eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        Pixelmon.EVENT_BUS.register(eventHandler);
    }

    // ... (o resto do arquivo continua igual) ...

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRanked());
        event.registerServerCommand(new CommandRanked());
        event.registerServerCommand(new CommandRankedAdmin());
    }

    @Mod.EventHandler
    public void onServerStop(FMLServerStoppingEvent event) {
        LOGGER.info("Fechando a conexao com o banco de dados...");
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static EventHandler getEventHandler() {
        return eventHandler;
    }
}
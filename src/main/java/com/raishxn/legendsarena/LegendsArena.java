package com.raishxn.legendsarena;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.raishxn.legendsarena.capabilities.CapabilityRegistry;
import com.raishxn.legendsarena.commands.CommandRanked;
import com.raishxn.legendsarena.events.EventHandler;
import com.raishxn.legendsarena.legendsarena.Tags;
import com.raishxn.legendsarena.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class LegendsArena {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    private static EventHandler eventHandler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);

        PacketHandler.registerMessages();
        CapabilityRegistry.register();

        eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        Pixelmon.EVENT_BUS.register(eventHandler);
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRanked());
    }

    // Método estático para acessar o EventHandler
    public static EventHandler getEventHandler() {
        return eventHandler;
    }
}
package com.envyful.api.forge.player;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Forge implementation of the {@link EnvyPlayer} interface
 *
 */
public class ForgeEnvyPlayer implements EnvyPlayer<EntityPlayerMP> {

    protected final Map<Class<?>, PlayerAttribute<?>> attributes = Maps.newHashMap();

    private EntityPlayerMP player;

    protected ForgeEnvyPlayer(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return this.player.getUniqueID();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public EntityPlayerMP getParent() {
        return this.player;
    }

    public void setPlayer(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void message(Object... messages) {
        for (Object message : messages) {
            if (message instanceof String) {
                this.player.sendMessage(new TextComponentString(UtilChatColour.translateColourCodes('&', (String) message)));
            } else if (message instanceof ITextComponent) {
                this.player.sendMessage((ITextComponent) message);
            } else if (message instanceof List) {
                for (Object subMessage : ((List) message)) {
                    this.message(subMessage);
                }
            } else {
                throw new RuntimeException("Unsupported message type");
            }
        }
    }

    @Override
    public void executeCommands(String... commands) {
        for (String command : commands) {
            this.executeCommand(command);
        }
    }

    @Override
    public void executeCommand(String command) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(this.player, command);
    }

    @Override
    public void teleport(ConfigLocation location) {
        //TODO:
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends PlayerAttribute<B>, B> A getAttribute(Class<B> plugin) {
        return (A) this.attributes.get(plugin);
    }
}

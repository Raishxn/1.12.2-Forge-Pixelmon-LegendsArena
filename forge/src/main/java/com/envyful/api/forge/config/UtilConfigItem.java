package com.envyful.api.forge.config;

import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.items.ItemFlag;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.gui.Transformer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.type.Pair;
import com.envyful.api.type.UtilParse;
import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class UtilConfigItem {

    public static void addPermissibleConfigItem(Pane pane, EntityPlayerMP player, List<Transformer> transformers, ExtendedConfigItem configItem) {
        addPermissibleConfigItem(pane, player, configItem, transformers,null);
    }

    public static void addPermissibleConfigItem(Pane pane, EntityPlayerMP player, ExtendedConfigItem configItem) {
        addPermissibleConfigItem(pane, player, configItem, null);
    }

    public static void addPermissibleConfigItem(Pane pane, EntityPlayerMP player, ExtendedConfigItem configItem,
                                                BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler) {
        addPermissibleConfigItem(pane, player, configItem, Collections.emptyList(), clickHandler);
    }

    public static void addPermissibleConfigItem(Pane pane, EntityPlayerMP player, ExtendedConfigItem configItem,
                                                List<Transformer> transformers,
                                                BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler) {
        ItemStack itemStack = fromPermissibleItem(player, configItem, transformers);

        if (itemStack == null) {
            return;
        }

        for (Pair<Integer, Integer> position : configItem.getPositions()) {
            if (clickHandler == null) {
                pane.set(position.getX(), position.getY(), GuiFactory.displayable(itemStack));
            } else {
                pane.set(position.getX(), position.getY(), GuiFactory.displayableBuilder(itemStack)
                        .clickHandler(clickHandler).build());
            }
        }
    }

    public static void addConfigItem(Pane pane, ExtendedConfigItem configItem) {
        addConfigItem(pane, configItem, null);
    }

    public static void addConfigItem(Pane pane, List<Transformer> transformers, ExtendedConfigItem configItem) {
        addConfigItem(pane, configItem, transformers,null);
    }

    public static void addConfigItem(Pane pane, ExtendedConfigItem configItem,
                                     BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler) {
        addConfigItem(pane, configItem, Collections.emptyList(), clickHandler);
    }

    public static void addConfigItem(Pane pane, ExtendedConfigItem configItem, List<Transformer> transformers,
                                     BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler) {
        if (!configItem.isEnabled()) {
            return;
        }

        for (Pair<Integer, Integer> position : configItem.getPositions()) {
            if (clickHandler == null) {
                pane.set(position.getX(), position.getY(), GuiFactory.displayable(fromConfigItem(
                        configItem,
                        transformers
                )));
            } else {
                pane.set(position.getX(), position.getY(), GuiFactory.displayableBuilder(fromConfigItem(
                        configItem,
                        transformers
                )).clickHandler(clickHandler).build());
            }
        }
    }

    public static ItemStack fromPermissibleItem(EntityPlayerMP player, ExtendedConfigItem permissibleConfigItem) {
        return fromPermissibleItem(player, permissibleConfigItem, Collections.emptyList());
    }

    public static ItemStack fromPermissibleItem(EntityPlayerMP player, ExtendedConfigItem permissibleConfigItem, List<Transformer> transformers) {
        if (!permissibleConfigItem.isEnabled()) {
            return null;
        }

        if (permissibleConfigItem.getPermission().isEmpty() || !permissibleConfigItem.requiresPermission() ||
                UtilPlayer.hasPermission(player, permissibleConfigItem.getPermission())) {
            return fromConfigItem(permissibleConfigItem);
        }

        if (permissibleConfigItem.getElseItem() == null || !permissibleConfigItem.getElseItem().isEnabled()) {
            return null;
        }

        return fromConfigItem(permissibleConfigItem.getElseItem());
    }

    public static ItemStack fromConfigItem(ExtendedConfigItem configItem, Transformer... transformers) {
        return fromConfigItem(configItem, Lists.newArrayList(transformers));
    }

    public static ItemStack fromConfigItem(ExtendedConfigItem configItem, List<Transformer> transformers) {
        return fromConfigItem(configItem.asConfigItem(), transformers);
    }

    public static ItemStack fromConfigItem(ConfigItem configItem, Transformer... transformers) {
        return fromConfigItem(configItem, Lists.newArrayList(transformers));
    }

    public static ItemStack fromConfigItem(ConfigItem configItem, List<Transformer> transformers) {
        if (!configItem.isEnabled()) {
            return null;
        }

        String name = configItem.getName();

        ItemBuilder itemBuilder = new ItemBuilder()
                .type(Item.getByNameOrId(configItem.getType()))
                .amount(configItem.getAmount(transformers))
                .damage(configItem.getDamage(transformers));

        List<String> lore = configItem.getLore();
        List<String> flags = configItem.getFlags();

        if (!transformers.isEmpty()) {
            for (Transformer transformer : transformers) {
                lore = transformer.transformLore(lore);
                name = transformer.transformName(name);
                flags = transformer.transformLore(flags);
            }
        }

        for (String flag : flags) {
            ItemFlag foundFlag = ItemFlag.valueOf(flag.toUpperCase());
            itemBuilder.itemFlag(foundFlag);
        }

        for (String s : lore) {
            itemBuilder.addLore(UtilChatColour.translateColourCodes('&', s));
        }

        itemBuilder.name(UtilChatColour.translateColourCodes('&', name));

        for (ConfigItem.EnchantData value : configItem.getEnchants().values()) {
            String enchantName = value.getEnchant();
            String level = value.getLevel();

            if (!transformers.isEmpty()) {
                for (Transformer transformer : transformers) {
                    enchantName = transformer.transformName(enchantName);
                    level = transformer.transformName(level);
                }
            }

            Enchantment enchantment = Enchantment.getEnchantmentByLocation(enchantName);
            int parsedLevel = UtilParse.parseInteger(level).orElse(1);

            if (enchantment == null) {
                continue;
            }

            itemBuilder.enchant(enchantment, parsedLevel);
        }

        for (Map.Entry<String, ConfigItem.NBTValue> nbtData : configItem.getNbt().entrySet()) {
            Pair<String, NBTBase> parsed = parseNBT(nbtData, transformers);

            if (parsed != null) {
                itemBuilder.nbt(parsed.getX(), parsed.getY());
            }
        }

        return itemBuilder.build();
    }

    public static Pair<String, NBTBase> parseNBT(Map.Entry<String, ConfigItem.NBTValue> nbtEntry, List<Transformer> transformers) {
        if (nbtEntry.getValue().getType().equalsIgnoreCase("nbt")) {
            NBTTagCompound compound = new NBTTagCompound();

            for (Map.Entry<String, ConfigItem.NBTValue> entry : nbtEntry.getValue().getSubData().entrySet()) {
                Pair<String, NBTBase> parsed = parseNBT(entry, transformers);

                if (parsed != null) {
                    compound.setTag(parsed.getX(), parsed.getY());
                }
            }

            return Pair.of(nbtEntry.getKey(), compound);
        }

        if (nbtEntry.getValue().getType().equalsIgnoreCase("list")) {
            NBTTagList list = new NBTTagList();

            for (Map.Entry<String, ConfigItem.NBTValue> nbtValue : nbtEntry.getValue().getSubData().entrySet()) {
                Pair<String, NBTBase> parsed = parseNBT(nbtValue, transformers);

                if (parsed != null) {
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setTag(parsed.getX(), parsed.getY());
                    list.appendTag(compound);
                }
            }

            return Pair.of(nbtEntry.getKey(), list);
        }

        return Pair.of(nbtEntry.getKey(), parseBasic(nbtEntry.getValue(), transformers));
    }

    public static NBTBase parseBasic(ConfigItem.NBTValue value, List<Transformer> transformers) {
        String data = value.getData();

        if (!transformers.isEmpty()) {
            for (Transformer transformer : transformers) {
                data = transformer.transformName(data);
            }
        }

        NBTBase base;

        switch (value.getType().toLowerCase()) {
            case "int":
            case "integer":
                base = new NBTTagInt(Integer.parseInt(data));
                break;
            case "long":
                base = new NBTTagLong(Long.parseLong(data));
                break;
            case "byte":
                base = new NBTTagByte(Byte.parseByte(data));
                break;
            case "double":
                base = new NBTTagDouble(Double.parseDouble(data));
                break;
            case "float":
                base = new NBTTagFloat(Float.parseFloat(data));
                break;
            case "short":
                base = new NBTTagShort(Short.parseShort(data));
                break;
            default:
            case "string":
                base = new NBTTagString(data);
                break;
        }

        return base;
    }
}

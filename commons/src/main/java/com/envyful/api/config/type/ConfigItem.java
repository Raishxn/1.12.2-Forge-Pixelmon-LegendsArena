package com.envyful.api.config.type;

import com.envyful.api.gui.Transformer;
import com.envyful.api.type.UtilParse;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * A serializable object that can be used to represent an Item in a config
 *
 */
@ConfigSerializable
public class ConfigItem {

    private boolean enabled = true;
    private String type = "minecraft:stained_glass_pane";
    private String amount = "1";
    private String damage;
    private String name = " ";
    private List<String> flags = Lists.newArrayList();
    private List<String> lore = Lists.newArrayList();
    private Map<String, EnchantData> enchants = Maps.newHashMap();
    private Map<String, NBTValue> nbt = Maps.newHashMap();

    @Deprecated
    public ConfigItem() {}

    @Deprecated
    public ConfigItem(boolean enabled, String type, String amount, String damage, String name, List<String> flags, List<String> lore, Map<String, EnchantData> enchants, Map<String, NBTValue> nbt) {
        this.enabled = enabled;
        this.type = type;
        this.amount = amount;
        this.damage = damage;
        this.name = name;
        this.flags = flags;
        this.lore = lore;
        this.enchants = enchants;
        this.nbt = nbt;
    }

    @Deprecated
    public ConfigItem(boolean enabled, String type, String amount, String name, List<String> flags, List<String> lore, Map<String, EnchantData> enchants, Map<String, NBTValue> nbt) {
        this.enabled = enabled;
        this.type = type;
        this.amount = amount;
        this.name = name;
        this.flags = flags;
        this.lore = lore;
        this.enchants = enchants;
        this.nbt = nbt;
    }

    @Deprecated
    public ConfigItem(String type, int amount, byte damage, String name, List<String> lore, Map<String, EnchantData> enchants, Map<String, NBTValue> nbt) {
        this.type = type;
        this.amount = amount + "";
        this.damage = damage + "";
        this.name = name;
        this.lore = lore;
        this.enchants = enchants;
        this.nbt = nbt;
    }

    @Deprecated
    public ConfigItem(String type, int amount, String name, List<String> lore, Map<String, EnchantData> enchants, Map<String, NBTValue> nbt) {
        this(type, amount, (byte) 0, name, lore, enchants, nbt);
    }

    @Deprecated
    public ConfigItem(String type, int amount, String name, List<String> lore) {
        this(type, amount, (byte) 0, name, lore, ImmutableMap.of(), ImmutableMap.of());
    }

    @Deprecated
    public ConfigItem(String type, int amount, byte damage, String name, List<String> lore, Map<String, NBTValue> nbt) {
        this(type, amount, damage, name, lore, Maps.newHashMap(), nbt);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getType() {
        return this.type;
    }

    public int getAmount() {
        return UtilParse.parseInteger(this.amount).orElse(0);
    }

    public int getAmount(List<Transformer> transformers) {
        String amount = this.amount;
        for (Transformer transformer : transformers) {
            amount = transformer.transformName(amount);
        }

        return UtilParse.parseInteger(amount).orElse(0);
    }

    public byte getDamage() {
        return (byte) (int) UtilParse.parseInteger(this.damage).orElse(0);
    }

    public byte getDamage(List<Transformer> transformers) {
        String damage = this.damage;
        for (Transformer transformer : transformers) {
            damage = transformer.transformName(damage);
        }

        return (byte) (int) UtilParse.parseInteger(damage).orElse(0);
    }

    public String getName() {
        return this.name;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public Map<String, EnchantData> getEnchants() {
        return enchants;
    }

    public List<String> getFlags() {
        return flags;
    }

    public Map<String, NBTValue> getNbt() {
        return this.nbt;
    }

    public static Builder builder() {
        return new Builder();
    }

    @ConfigSerializable
    public static final class NBTValue {

        private String type;
        private String data;
        private Map<String, NBTValue> subData;

        public NBTValue() {}

        public NBTValue(String type, String data) {
            this.type = type;
            this.data = data;
        }

        public NBTValue(String type, Map<String, NBTValue> subData) {
            this.type = type;
            this.subData = subData;
        }

        public String getType() {
            return this.type;
        }

        public String getData() {
            return this.data;
        }

        public Map<String, NBTValue> getSubData() {
            return subData;
        }
    }

    @ConfigSerializable
    public static final class EnchantData {

        private String enchant;
        private String level;

        public EnchantData() {}

        public EnchantData(String enchant, String level) {
            this.enchant = enchant;
            this.level = level;
        }

        public String getEnchant() {
            return this.enchant;
        }

        public String getLevel() {
            return this.level;
        }
    }

    public static class Builder {

        private boolean enabled = true;
        private String type = "minecraft:stained_glass_pane";
        private String amount = "1";
        private String damage;
        private String name = " ";
        private List<String> flags = Lists.newArrayList();
        private List<String> lore = Lists.newArrayList();
        private Map<String, EnchantData> enchants = Maps.newHashMap();
        private Map<String, NBTValue> nbt = Maps.newHashMap();

        protected Builder() {}

        public Builder enabled() {
            this.enabled = true;
            return this;
        }

        public Builder disabled() {
            this.enabled = false;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = String.valueOf(amount);
            return this;
        }

        public Builder damage(double damage) {
            this.damage = String.valueOf(damage);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder flags(String... flags) {
            this.flags.addAll(Arrays.asList(flags));
            return this;
        }

        public Builder lore(String... lore) {
            this.lore.addAll(Arrays.asList(lore));
            return this;
        }

        public Builder enchants(EnchantData... enchants) {
            for (EnchantData enchant : enchants) {
                this.enchants.put("enchant-" + this.enchants.size(), enchant);
            }

            return this;
        }

        public Builder nbt(String key, NBTValue value) {
            this.nbt.put(key, value);
            return this;
        }

        public ConfigItem build() {
            return new ConfigItem(this.enabled, this.type, this.amount, this.damage, this.name, this.flags, this.lore, this.enchants, this.nbt);
        }
    }
}

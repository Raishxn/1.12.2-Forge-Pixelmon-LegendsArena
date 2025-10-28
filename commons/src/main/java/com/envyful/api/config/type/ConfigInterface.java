package com.envyful.api.config.type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;

/**
 *
 * A serializable object that can be used to represent basic settings for a GUI
 *
 */
@ConfigSerializable
public class ConfigInterface {

    private String title = "";
    private int height = 6;
    private String fillType = FillType.BLOCK.name();
    private Map<String, ConfigItem> fillerItems = Maps.newHashMap(ImmutableMap.of("one", new ConfigItem()));

    public ConfigInterface() {}

    public ConfigInterface(String title, int height, String fillType, Map<String, ConfigItem> fillerItems) {
        this.title = title;
        this.height = height;
        this.fillType = fillType;
        this.fillerItems = fillerItems;
    }

    public String getTitle() {
        return this.title;
    }

    public int getHeight() {
        return this.height;
    }

    public FillType getFillType() {
        return FillType.valueOf(this.fillType);
    }

    public List<ConfigItem> getFillerItems() {
        return this.getFillType().convert(Lists.newArrayList(this.fillerItems.values()), this.getHeight());
    }

    public enum FillType {

        BLOCK() {
            @Override
            public List<ConfigItem> convert(List<ConfigItem> conversion, int height) {
                List<ConfigItem> configItems = Lists.newArrayList();
                ConfigItem primary = conversion.get(0);

                for(int y = 0; y < height; y++) {
                    for (int x = 0; x < 9; x++) {
                        configItems.add(primary);
                    }
                }

                return configItems;
            }
        },
        CUSTOM() {
            @Override
            public List<ConfigItem> convert(List<ConfigItem> conversion, int height) {
                return conversion;
            }
        },
        ALTERNATING() {
            @Override
            public List<ConfigItem> convert(List<ConfigItem> conversion, int height) {
                List<ConfigItem> configItems = Lists.newArrayList();
                ConfigItem primary = conversion.get(0);
                ConfigItem secondary = conversion.get(1);

                for(int y = 0; y < height; y++) {
                    for (int x = 0; x < 9; x++) {
                        if (x % 2 == 0) {
                            configItems.add(primary);
                        } else {
                            configItems.add(secondary);
                        }
                    }
                }

                return configItems;
            }
        },
        CHECKERED() {
            @Override
            public List<ConfigItem> convert(List<ConfigItem> conversion, int height) {
                List<ConfigItem> configItems = Lists.newArrayList();
                ConfigItem primary = conversion.get(0);
                ConfigItem secondary = conversion.get(1);

                for(int y = 0; y < height; y++) {
                    for (int x = 0; x < 9; x++) {
                        if ((x + (y  % 2)) % 2 == 0) {
                            configItems.add(primary);
                        } else {
                            configItems.add(secondary);
                        }
                    }
                }

                return configItems;
            }
        }

        ;

        public abstract List<ConfigItem> convert(List<ConfigItem> conversion, int height);
    }
}

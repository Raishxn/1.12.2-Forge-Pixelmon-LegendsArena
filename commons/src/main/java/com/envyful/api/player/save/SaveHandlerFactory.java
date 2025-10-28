package com.envyful.api.player.save;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 *
 * Static factory for storing all {@link VariableSaveHandler} for the annotation {@link com.envyful.api.player.save.attribute.SaveHandler}
 *
 */
public class SaveHandlerFactory {

    private static final Map<Class<? extends VariableSaveHandler<?>>, VariableSaveHandler<?>> SAVE_HANDLERS = Maps.newHashMap();

    /**
     *
     * Registers a {@link VariableSaveHandler}
     *
     * @param saveHandler The registered handler
     */
    public static void register(VariableSaveHandler<?> saveHandler) {
        SAVE_HANDLERS.put((Class<? extends VariableSaveHandler<?>>) saveHandler.getClass(), saveHandler);
    }

    /**
     *
     * Searches for a {@link VariableSaveHandler} through registered classes
     * Returns null if not registered
     *
     * @param clazz The class searching for
     * @return The registered save handler
     */
    public static VariableSaveHandler<?> getSaveHandler(Class<? extends VariableSaveHandler<?>> clazz) {
        return SAVE_HANDLERS.get(clazz);
    }
}

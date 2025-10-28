package com.envyful.api.player.save;

import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;

import java.util.List;
import java.util.UUID;

/**
 *
 * Used for handling the saving and loading of attribute data
 *
 * @param <T> The player type
 */
public interface SaveManager<T> {

    /**
     *
     * Registers an attribute for how this class should handle saving & loading
     *
     * @param manager The manager object
     * @param attribute The class of the attribute being registered
     */
    void registerAttribute(Object manager, Class<? extends PlayerAttribute<?>> attribute);

    /**
     *
     * Saves the player's data from the given attribute
     * This will call {@link PlayerAttribute#save()} if this class has not been registered using
     * {@link SaveManager#registerAttribute(Object, Class)}
     *
     * This calls {@link PlayerAttribute#preSave()} before saving the data and {@link PlayerAttribute#postSave()}
     * after saving the data
     *
     * @param player The player who's data is being saved
     * @param attribute The attribute being saved
     */
    void saveData(EnvyPlayer<T> player, PlayerAttribute<?> attribute);

    /**
     *
     * Saves the player's data from the given attribute
     *
     * @param uuid The offline UUID
     * @param attribute The attribute being saved
     */
    void saveData(UUID uuid, PlayerAttribute<?> attribute);

    /**
     *
     * Load the player's data for all registered {@link PlayerAttribute} using
     *{@link SaveManager#registerAttribute(Object, Class)}
     *
     * This calls {@link PlayerAttribute#preLoad()} before loading the data and {@link PlayerAttribute#postLoad()}
     * after loading the data
     *
     * @param player The player who's data is being loaded
     * @return All successfully loaded attributes
     */
    List<PlayerAttribute<?>> loadData(EnvyPlayer<T> player);

    /**
     *
     * Load the offline player's data for all registerd {@link PlayerAttribute} using {@link SaveManager#registerAttribute(Object, Class)}
     *
     * This calls {@link PlayerAttribute#preLoad()} before loading the data and {@link PlayerAttribute#postLoad()}
     * after loading the data
     *
     * @param uuid The offline player's UUID
     * @return All successfully loaded attributes
     */
    List<PlayerAttribute<?>> loadData(UUID uuid);

}

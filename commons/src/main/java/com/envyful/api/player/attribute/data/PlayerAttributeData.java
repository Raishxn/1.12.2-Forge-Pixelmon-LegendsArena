package com.envyful.api.player.attribute.data;

import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * A simple object used for efficiently instantiating the {@link PlayerAttribute} classes using reflection.
 *
 * The constructor of the {@link PlayerAttribute} is stored for future efficiency and thus allowing for easy attribute
 * instantiation
 *
 */
public class PlayerAttributeData {

    private final Object manager;
    private final Class<?> managerClass;
    private final Class<? extends PlayerAttribute<?>> attributeClass;
    private final Constructor<? extends PlayerAttribute<?>> constructor;

    /**
     *
     * Passing the manager and attribute class allows for the object to get the necessary classes, and constructors
     * using this information for later instantiation
     *
     * @param manager The manager object
     * @param attributeClass The class of the attribute being stored
     */
    public PlayerAttributeData(Object manager, Class<? extends PlayerAttribute<?>> attributeClass) {
        this.manager = manager;
        this.managerClass = this.manager.getClass();
        this.attributeClass = attributeClass;
        this.constructor = this.getConstructor();
    }

    public Class<? extends PlayerAttribute<?>> getAttributeClass() {
        return this.attributeClass;
    }

    private Constructor<? extends PlayerAttribute<?>> getConstructor() {
        try {
            return attributeClass.getConstructor(this.managerClass, EnvyPlayer.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * Creates a new instanceof the {@link PlayerAttribute} for the {@link EnvyPlayer} passed
     * {@link PlayerAttribute#load} not called here
     *
     * @param player The parent of the new attribute
     * @return The new attribute
     */
    public PlayerAttribute<?> getInstance(EnvyPlayer<?> player) {
        try {
            return this.constructor.newInstance(this.manager, player);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * Adds the instance to the given map to ensure encapsulation is followed.
     *
     * @param map The map being added to
     * @param instance The instance being added to the map using the manager class as a key
     */
    public void addToMap(Map<Class<?>, PlayerAttribute<?>> map, PlayerAttribute<?> instance) {
        map.put(this.managerClass, instance);
    }
}

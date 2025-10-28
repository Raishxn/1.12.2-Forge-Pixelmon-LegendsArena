package com.envyful.api.player;

import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.save.SaveManager;

import java.util.List;
import java.util.UUID;

/**
 *
 * An interface designed to store local user objects for the plugin using it.
 * Should handle all login & logout logic (reducing boilerplate / duplicate code) allowing for easy caching
 * with minimal code at the implementation level.
 *
 * Check the implementation level module (i.e. forge module) for details on how to create an instance of this interface.
 *
 * @param <A> The generic for the API's player object
 * @param <B> The generic parameter for the platform's player object
 */
public interface PlayerManager<A extends EnvyPlayer<B>, B> {

     /**
      *
      * Get the {@link EnvyPlayer} implementation from the platform's player implementation
      *
      * Will return null if the player is not online
      *
      * @param player The platform's player object
      * @return The API's player implementation
      */
     A getPlayer(B player);

     /**
      *
      * Get the {@link EnvyPlayer} implementation from the minecraft player's UUID
      *
      * Will return null if the player is not online
      *
      * @param uuid The minecraft player's UUID
      * @return The API's player implementation
      */
     A getPlayer(UUID uuid);

     /**
      *
      * Gets the {@link EnvyPlayer} implementation from the player's username (if they are online)
      *
      * Will return null if the player is not online
      *
      * @param username The username of the minecraft player
      * @return The API's player implementation
      */
     A getOnlinePlayer(String username);


     /**
      *
      * Gets the {@link EnvyPlayer} implementation from the player's username (if they are online) (case insensitive)
      *
      * Will return null if the player is not online
      *
      * @param username The username of the minecraft player
      * @return The API's player implementation
      */
     A getOnlinePlayerCaseInsensitive(String username);

     /**
      *
      * Gets a {@link List} of all online players in the {@link EnvyPlayer} form
      *
      *
      * @return All online players
      */
     List<A> getOnlinePlayers();

     /**
      *
      * Gets the registered attributes for an offline player
      * Will return an empty list if the player is not found
      *
      * @param uuid The uuid of the target player
      * @return The attributes of said offline player
      */
     List<PlayerAttribute<?>> getOfflineAttributes(UUID uuid);

     /**
      *
      * Registers an {@link com.envyful.api.player.attribute.PlayerAttribute) class so that when the player object is
      * instantiated it can be created (using reflection) from the registry in the PlayerManager implementation.
      *
      * If {@link PlayerManager#setSaveManager(SaveManager)} has been called then it will call {
      * @link SaveManager#registerAttribute(Class)} on the given class
      *
      * @param manager The manager object to be passed through the attribute constructor at instantiation
      * @param attribute The class of the attribute being registered
      */
     void registerAttribute(Object manager, Class<? extends PlayerAttribute<?>> attribute);

     /**
      *
      * Sets the player manager's {@link SaveManager}
      *
      * @param saveManager The new save manager
      */
     void setSaveManager(SaveManager<B> saveManager);

}

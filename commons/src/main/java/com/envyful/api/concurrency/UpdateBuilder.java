package com.envyful.api.concurrency;

import com.envyful.api.json.UtilGson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.*;

/**
 *
 * Basic non-platform specific implementation of a GitHub update checker.
 * This will ping the releases on the given repo every 10_000_000 milliseconds to determine if the current version
 * is the latest release.
 *
 * If it's not the latest release admins will be notified upon joining.
 *
 * @param <T> The player type
 */
public class UpdateBuilder<T> {

    private String name;
    private String requiredPermission;
    private String owner;
    private String repo;
    private String version;

    protected boolean upToDate = true;

    protected UpdateBuilder() {}

    /**
     *
     * Sets the name of the mod/plugin
     *
     * @param name The name
     * @return The builder
     */
    public UpdateBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    /**
     *
     * Sets the required permission for the player to receive the update
     *
     * @param requiredPermission The permission required
     * @return The builder
     */
    public UpdateBuilder<T> requiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
        return this;
    }

    /**
     *
     * Sets the GitHub owner of the project
     *
     * @param owner The owner
     * @return The builder
     */
    public UpdateBuilder<T> owner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     *
     * Sets the repo of the GitHub project
     *
     * @param repo The repo
     * @return The builder
     */
    public UpdateBuilder<T> repo(String repo) {
        this.repo = repo;
        return this;
    }

    /**
     *
     * Sets the current version of the mod / plugin
     *
     * @param version The version
     * @return The builder
     */
    public UpdateBuilder<T> version(String version) {
        this.version = version;
        return this;
    }

    /**
     *
     * Attempts to send the update message to the player
     *
     * @param player The player
     * @param permissionCheck The method to check if they have the permission
     * @param messageSender The method to send the message
     */
    protected void attemptSendMessage(T player, BiPredicate<T, String> permissionCheck, BiConsumer<T, String> messageSender) {
        if (this.requiredPermission.isEmpty() || permissionCheck.test(player, this.requiredPermission)) {
            messageSender.accept(player, "§c§lUPDATE REQUIRED: §c" + this.name + " is not up to date! Check " +
                    "§nhttps://github.com/" + this.owner + "/" + this.repo  + "/releases/");
        }
    }


    /**
     *
     * Starts the update checking thread & registeres required listeners
     *
     */
    public void start() {
        UtilConcurrency.runAsync(() -> {
            while (true) {
                try {
                    this.sendRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendRequest() throws IOException {
        URL url = new URL("https://api.github.com/repos/" + this.owner + "/" + this.repo + "/releases/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            System.out.println("ERROR: " + connection.getResponseCode());
            return;
        }

        LinkedTreeMap<String, Object> data = UtilGson.GSON.fromJson(new InputStreamReader(connection.getInputStream()), LinkedTreeMap.class);

        if (!data.get("tag_name").equals(this.version)) {
            this.upToDate = false;
        }
    }
}

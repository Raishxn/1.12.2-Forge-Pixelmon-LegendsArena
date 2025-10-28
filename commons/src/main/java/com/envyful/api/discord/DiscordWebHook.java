package com.envyful.api.discord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Class for handling simple Discord webhooks
 * Originally from:
 * <a>https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb</a>
 *
 */
public class DiscordWebHook {

    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private List<EmbedObject> embeds = new ArrayList<>();

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebHook(String url) {
        this.url = url;
    }

    /**
     *
     * Sets the text content of the message
     *
     * @param content The text content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     *
     * Sets the username of the sender
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * Sets the URL of the avatar for the sending user
     *
     * @param avatarUrl the avatar url
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     *
     * Sets if the message should be read as TTS to anyone in a voice channel reading the given text channel
     *
     * @param tts if it should be TTS
     */
    public void setTts(boolean tts) {
        this.tts = tts;
    }

    /**
     *
     * Adds an embed to the message being sent
     *
     * @param embed The embed being added
     */
    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    /**
     *
     * Gets a new instance of the Builder class
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     * Clones current web hook to a new object
     *
     * @return new webhook
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DiscordWebHook clone() {
        DiscordWebHook webHook = new DiscordWebHook(this.url);
        webHook.setUsername(this.username);
        webHook.setAvatarUrl(this.avatarUrl);
        webHook.setTts(this.tts);

        for (EmbedObject embed : this.embeds) {
            webHook.addEmbed(embed);
        }

        return webHook;
    }

    /**
     *
     * Executes the message and sends it to the web hook URL
     *
     * @throws IOException Exception when something is incorrect or goes wrong
     */
    public void execute() throws IOException {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        JSONObject json = this.toJson();

        URL url = new URL(this.url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        OutputStream stream = connection.getOutputStream();
        stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
        stream.flush();
        stream.close();

        connection.getInputStream().close(); //I'm not sure why but it doesn't work without getting the InputStream
        connection.disconnect();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);

        if (!this.embeds.isEmpty()) {
            List<JSONObject> embedObjects = new ArrayList<>();

            for (EmbedObject embed : this.embeds) {
                embedObjects.add(embed.toJson());
            }

            json.put("embeds", embedObjects.toArray());
        }

        return json;
    }

    /**
     *
     * Builds a webhook from a JSON string
     *
     * @param json The json string
     * @return The new webhook
     */
    public static DiscordWebHook fromJson(String json) {
        JsonObject jsonElement = JsonParser.parseString(json).getAsJsonObject();
        Builder builder = DiscordWebHook.builder();

        if (jsonElement.has("url")) {
            builder.url(jsonElement.get("url").getAsString());
        }

        if (jsonElement.has("content")) {
            builder.content(jsonElement.get("content").getAsString());
        }

        if (jsonElement.has("username")) {
            builder.username(jsonElement.get("username").getAsString());
        }

        if (jsonElement.has("avatar_url")) {
            builder.avatarUrl(jsonElement.get("avatar_url").getAsString());
        }

        if (jsonElement.has("tts")) {
            builder.tts(jsonElement.get("tts").getAsBoolean());
        }

        if (jsonElement.has("embeds")) {
            for (JsonElement embeds : jsonElement.get("embeds").getAsJsonArray()) {
                JsonObject asJsonObject = embeds.getAsJsonObject();
                builder.embeds(EmbedObject.fromJson(asJsonObject.toString()));
            }
        }

        return builder.build();
    }

    /**
     *
     * Static builder class for the Web hook
     *
     */
    public static class Builder {

        private String url;
        private String content;
        private String username;
        private String avatarUrl;
        private boolean tts;
        private List<EmbedObject> embeds = new ArrayList<>();

        Builder() {}

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder tts(boolean tts) {
            this.tts = tts;
            return this;
        }

        public Builder embeds(EmbedObject... embeds) {
            this.embeds.addAll(Arrays.asList(embeds));
            return this;
        }

        public DiscordWebHook build() {
            DiscordWebHook webHook = new DiscordWebHook(this.url);
            webHook.setContent(this.content);
            webHook.setUsername(this.username);
            webHook.setAvatarUrl(this.avatarUrl);
            webHook.setTts(this.tts);

            for (EmbedObject embed : this.embeds) {
                webHook.addEmbed(embed);
            }

            return webHook;
        }
    }

}

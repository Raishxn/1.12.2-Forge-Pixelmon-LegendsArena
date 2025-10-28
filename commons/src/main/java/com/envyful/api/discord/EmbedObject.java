package com.envyful.api.discord;

import com.envyful.api.json.UtilGson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmbedObject {
    private String title;
    private String description;
    private String url;
    private Color color;

    private Footer footer;
    private Thumbnail thumbnail;
    private Image image;
    private Author author;
    private List<Field> fields = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public Color getColor() {
        return color;
    }

    public Footer getFooter() {
        return footer;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public Image getImage() {
        return image;
    }

    public Author getAuthor() {
        return author;
    }

    public List<Field> getFields() {
        return fields;
    }

    public EmbedObject setTitle(String title) {
        this.title = title;
        return this;
    }

    public EmbedObject setDescription(String description) {
        this.description = description;
        return this;
    }

    public EmbedObject setUrl(String url) {
        this.url = url;
        return this;
    }

    public EmbedObject setColor(Color color) {
        this.color = color;
        return this;
    }

    public EmbedObject setFooter(String text, String icon) {
        this.footer = new Footer(text, icon);
        return this;
    }

    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    public EmbedObject setThumbnail(String url) {
        this.thumbnail = new Thumbnail(url);
        return this;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public EmbedObject setImage(String url) {
        this.image = new Image(url);
        return this;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public EmbedObject setAuthor(String name, String url, String icon) {
        this.author = new Author(name, url, icon);
        return this;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public EmbedObject addField(String name, String value, boolean inline) {
        this.fields.add(new Field(name, value, inline));
        return this;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     *
     * Converts the {@link EmbedObject} to a {@link JSONObject}
     *
     * @return the JSONified version of the embed
     */
    public JSONObject toJson() {
        JSONObject jsonEmbed = new JSONObject();

        jsonEmbed.put("title", this.getTitle());
        jsonEmbed.put("description", this.getDescription());
        jsonEmbed.put("url", this.getUrl());

        if (this.getColor() != null) {
            Color color = this.getColor();
            int rgb = color.getRed();
            rgb = (rgb << 8) + color.getGreen();
            rgb = (rgb << 8) + color.getBlue();

            jsonEmbed.put("color", rgb);
        }

        EmbedObject.Footer footer = this.getFooter();
        EmbedObject.Image image = this.getImage();
        EmbedObject.Thumbnail thumbnail = this.getThumbnail();
        EmbedObject.Author author = this.getAuthor();
        List<EmbedObject.Field> fields = this.getFields();

        if (footer != null) {
            JSONObject jsonFooter = new JSONObject();

            jsonFooter.put("text", footer.getText());
            jsonFooter.put("icon_url", footer.getIconUrl());
            jsonEmbed.put("footer", jsonFooter);
        }

        if (image != null) {
            JSONObject jsonImage = new JSONObject();

            jsonImage.put("url", image.getUrl());
            jsonEmbed.put("image", jsonImage);
        }

        if (thumbnail != null) {
            JSONObject jsonThumbnail = new JSONObject();

            jsonThumbnail.put("url", thumbnail.getUrl());
            jsonEmbed.put("thumbnail", jsonThumbnail);
        }

        if (author != null) {
            JSONObject jsonAuthor = new JSONObject();

            jsonAuthor.put("name", author.getName());
            jsonAuthor.put("url", author.getUrl());
            jsonAuthor.put("icon_url", author.getIconUrl());
            jsonEmbed.put("author", jsonAuthor);
        }

        List<JSONObject> jsonFields = new ArrayList<>();
        for (EmbedObject.Field field : fields) {
            JSONObject jsonField = new JSONObject();

            jsonField.put("name", field.getName());
            jsonField.put("value", field.getValue());
            jsonField.put("inline", field.isInline());

            jsonFields.add(jsonField);
        }

        jsonEmbed.put("fields", jsonFields.toArray());
        return jsonEmbed;
    }

    /**
     *
     * Converts a JSON string to an Embed
     *
     * @param json The json being converted
     * @return The new embed
     */
    public static EmbedObject fromJson(String json) {
        JsonObject jsonElement = JsonParser.parseString(json).getAsJsonObject();
        EmbedObject embedObject = new EmbedObject();

        if (jsonElement.has("title")) {
            embedObject.setTitle(jsonElement.get("title").getAsString());
        }

        if (jsonElement.has("description")) {
            embedObject.setDescription(jsonElement.get("description").getAsString());
        }

        if (jsonElement.has("url")) {
            embedObject.setUrl(jsonElement.get("url").getAsString());
        }

        if (jsonElement.has("color")) {
            int rgb = jsonElement.get("color").getAsInt();
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            embedObject.setColor(new Color(red, green, blue));
        }

        if (jsonElement.has("footer")) {
            JsonObject footer = jsonElement.get("footer").getAsJsonObject();
            embedObject.setFooter(footer.get("text").getAsString(), footer.get("icon_url").getAsString());
        }

        if (jsonElement.has("image")) {
            JsonObject image = jsonElement.get("image").getAsJsonObject();
            embedObject.setImage(image.get("url").getAsString());
        }

        if (jsonElement.has("thumbnail")) {
            JsonObject thumbnail = jsonElement.get("thumbnail").getAsJsonObject();
            embedObject.setThumbnail(thumbnail.get("url").getAsString());
        }

        if (jsonElement.has("author")) {
            JsonObject author = jsonElement.get("author").getAsJsonObject();
            embedObject.setAuthor(author.get("name").getAsString(), author.get("url").getAsString(), author.get(
                    "icon_url").getAsString());
        }

        if (jsonElement.has("fields")) {
            JsonArray fields = jsonElement.get("fields").getAsJsonArray();

            for (JsonElement field : fields) {
                JsonObject fieldObject = field.getAsJsonObject();
                embedObject.addField(fieldObject.get("name").getAsString(), fieldObject.get("value").getAsString(),
                                     fieldObject.get("inline").getAsBoolean());
            }
        }

        return embedObject;
    }

    public static class Footer {
        private String text;
        private String iconUrl;

        public Footer(String text, String iconUrl) {
            this.text = text;
            this.iconUrl = iconUrl;
        }

        public String getText() {
            return text;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Thumbnail {
        private String url;

        public Thumbnail(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class Image {
        private String url;

        public Image(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class Author {
        private String name;
        private String url;
        private String iconUrl;

        public Author(String name, String url, String iconUrl) {
            this.name = name;
            this.url = url;
            this.iconUrl = iconUrl;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Field {
        private String name;
        private String value;
        private boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isInline() {
            return inline;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Builder {

        private String title;
        private String description;
        private String url;
        private Color color;

        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private List<Field> fields = new ArrayList<>();

        public Builder() {}

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder footer(Footer footer) {
            this.footer = footer;
            return this;
        }

        public Builder thumbnail(Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder image(Image image) {
            this.image = image;
            return this;
        }

        public Builder author(Author author) {
            this.author = author;
            return this;
        }

        public Builder fields(Field... fields) {
            this.fields.addAll(Arrays.asList(fields));
            return this;
        }

        public EmbedObject build() {
            EmbedObject embedObject = new EmbedObject();

            embedObject.setTitle(this.title);
            embedObject.setDescription(this.description);
            embedObject.setUrl(this.url);

            if (this.color != null) {
                embedObject.setColor(this.color);
            }

            if (this.footer != null) {
                embedObject.setFooter(this.footer);
            }

            if (this.thumbnail != null) {
                embedObject.setThumbnail(this.thumbnail);
            }

            if (this.image != null) {
                embedObject.setImage(this.image);
            }

            if (this.author != null) {
                embedObject.setAuthor(this.author);
            }

            if (!this.fields.isEmpty()) {
                for (Field field : this.fields) {
                    embedObject.addField(field);
                }
            }

            return embedObject;
        }
    }
}

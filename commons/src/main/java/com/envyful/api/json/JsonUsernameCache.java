package com.envyful.api.json;

import com.envyful.api.concurrency.AsyncTaskBuilder;
import com.google.common.collect.Maps;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class JsonUsernameCache {

    private final File usernameCache;
    private final Map<String, UUID> uuidByUsername = Maps.newConcurrentMap();
    private boolean saving = false;

    public JsonUsernameCache() {
        this(new File("config/players/users.json"));
    }

    public JsonUsernameCache(File usernameCache) {
        this.usernameCache = usernameCache;

        try {
            this.createFiles();
            this.loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new AsyncTaskBuilder()
                .delay(20 * 60 * 30L)
                .interval(20 * 60 * 30L)
                .task(this::save)
                .start();
    }

    private void createFiles() throws IOException {
        if (!this.usernameCache.getParentFile().exists()) {
            Files.createDirectories(this.usernameCache.getParentFile().toPath());
        }

        if (!this.usernameCache.exists()) {
            Files.createFile(this.usernameCache.toPath());
        }
    }

    private void loadData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader( this.usernameCache));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] args = line.split("@@##@@");
            this.uuidByUsername.put(args[0], new UUID(Long.parseLong(args[1]), Long.parseLong(args[2])));
        }

        reader.close();
    }

    public void addCache(UUID uuid, String name) {
        this.uuidByUsername.put(name, uuid);
    }

    public UUID getUUID(String name) {
        return this.uuidByUsername.get(name);
    }

    public void invalidateUUID(UUID uuid) {
        String oldName = this.findOldName(uuid);

        if (oldName != null)  {
            this.uuidByUsername.remove(oldName);
        }
    }

    private String findOldName(UUID uuid) {
        for (Map.Entry<String, UUID> entry : this.uuidByUsername.entrySet()) {
            if (Objects.equals(entry.getValue(), uuid)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public void setSaving(boolean saving) {
        this.saving = saving;
    }

    public void save() {
        this.saving = true;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.usernameCache));

            for (Map.Entry<String, UUID> entry : this.uuidByUsername.entrySet()) {
                if (!this.saving) {
                    return;
                }

                writer.write(entry.getKey() + "@@##@@" + entry.getValue().getMostSignificantBits() + "@@##@@" + entry.getValue().getLeastSignificantBits() + System.lineSeparator());
            }

            this.saving = false;
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

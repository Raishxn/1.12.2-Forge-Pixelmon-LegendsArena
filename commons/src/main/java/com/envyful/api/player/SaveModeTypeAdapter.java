package com.envyful.api.player;

import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class SaveModeTypeAdapter extends ScalarSerializer<SaveMode> {

    public SaveModeTypeAdapter() {
        super(SaveMode.class);
    }

    @Override
    public SaveMode deserialize(Type type, Object obj) throws SerializationException {
        return SaveMode.valueOf(obj.toString().toUpperCase());
    }

    @Override
    protected Object serialize(SaveMode item, Predicate<Class<?>> typeSupported) {
        return item.name();
    }
}

package com.envyful.api.player.save.impl;

import com.envyful.api.database.Database;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.save.SaveHandlerFactory;
import com.envyful.api.player.save.SaveManager;
import com.envyful.api.player.save.VariableSaveHandler;
import com.envyful.api.player.save.attribute.ColumnData;
import com.envyful.api.player.save.attribute.Queries;
import com.envyful.api.player.save.attribute.SaveHandler;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class SQLSaveManager<T> implements SaveManager<T> {

    private final Map<Class<? extends PlayerAttribute<?>>, AttributeData> loadedAttributes = Maps.newConcurrentMap();
    private final Database database;

    public SQLSaveManager(Database database) {this.database = database;}

    @Override
    public List<PlayerAttribute<?>> loadData(EnvyPlayer<T> player) {
        if (this.loadedAttributes.isEmpty()) {
            return Collections.emptyList();
        }

        List<PlayerAttribute<?>> attributes = Lists.newArrayList();

        for (Map.Entry<Class<? extends PlayerAttribute<?>>, AttributeData> entry : this.loadedAttributes.entrySet()) {
            AttributeData value = entry.getValue();
            PlayerAttribute<?> attribute = value.getConstructor().apply(player, value.getManager());

            attribute.preLoad();

            try (Connection connection = this.database.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(value.getQueries().loadQuery())) {
                Field[] fields = value.getFieldsPositions().get(value.getQueries().loadQuery());

                for (int i = 0; i < fields.length; i++) {
                    preparedStatement.setObject(i, fields[i].get(attribute));
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.next()) {
                    attribute.postLoad();
                    attributes.add(attribute);
                    continue;
                }

                for (Map.Entry<Field, FieldData> fieldData : value.getFieldData().entrySet()) {
                    FieldData data = fieldData.getValue();

                    if (data.getSaveHandler() != null) {
                        fieldData.getKey().set(attribute, data.getSaveHandler().invert(resultSet.getString(fieldData.getValue().getName())));
                    } else {
                        fieldData.getKey().set(attribute, resultSet.getObject(fieldData.getValue().getName()));
                    }
                }
            } catch (SQLException | IllegalAccessException e) {
                attribute.load();
                e.printStackTrace();
            }

            attribute.postLoad();
            attributes.add(attribute);
        }

        return attributes;
    }

    @Override
    public List<PlayerAttribute<?>> loadData(UUID uuid) {
        return null;
    }

    @Override
    public void saveData(EnvyPlayer<T> player, PlayerAttribute<?> attribute) {
        AttributeData attributeData = this.loadedAttributes.get(attribute.getClass());

        if (attributeData == null) {
            attribute.save();
            return;
        }

        attribute.preSave();

        try (Connection connection = this.database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(attributeData.getQueries().updateQuery())) {
            Field[] fieldPositions = attributeData.getFieldsPositions().get(attributeData.getQueries().updateQuery());

            for (int i = 0; i < fieldPositions.length; i++) {
                Field fieldPosition = fieldPositions[i];

                FieldData fieldData = attributeData.getFieldData().get(fieldPosition);

                if (fieldData.getSaveHandler() != null) {
                    preparedStatement.setString(i, fieldData.getSaveHandler().convert(fieldPosition.get(attribute)));
                } else {
                    preparedStatement.setObject(i, fieldPosition.get(attribute));
                }
            }

            preparedStatement.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }

        attribute.postSave();
    }

    @Override
    public void saveData(UUID uuid, PlayerAttribute<?> attribute) {

    }

    @Override
    public void registerAttribute(Object manager, Class<? extends PlayerAttribute<?>> attribute) {
        Map<Field, FieldData> fieldData = this.getFieldData(attribute);
        Queries queries = attribute.getAnnotation(Queries.class);

        if (queries == null) {
            return;
        }

        Map<String, Field[]> fieldsPositions = ImmutableMap.of(
                queries.loadQuery(), this.getFieldPositions(queries.loadQuery(), fieldData),
                queries.updateQuery(), this.getFieldPositions(queries.updateQuery(), fieldData)
        );

        BiFunction<EnvyPlayer<?>, Object, PlayerAttribute<?>> constructor = this.getConstructor(manager, attribute);

        this.loadedAttributes.put(attribute, new AttributeData(manager, constructor, queries, fieldData, fieldsPositions));
    }

    private Map<Field, FieldData> getFieldData(Class<? extends PlayerAttribute<?>> attribute) {
        Map<Field, FieldData> fieldData = Maps.newHashMap();

        for (Field declaredField : attribute.getDeclaredFields()) {
            if (Modifier.isTransient(declaredField.getModifiers())) {
                continue;
            }

            declaredField.setAccessible(true);
            ColumnData columnData = declaredField.getAnnotation(ColumnData.class);
            SaveHandler saveHandler = declaredField.getAnnotation(SaveHandler.class);
            String name;
            VariableSaveHandler<?> variableSaveHandler = null;

            if (columnData == null) {
                name = this.calculateColumnName(declaredField);
            } else {
                name = columnData.value();
            }

            if (saveHandler != null) {
                variableSaveHandler = SaveHandlerFactory.getSaveHandler(saveHandler.value());
            }

            fieldData.put(declaredField, new FieldData(declaredField, name, variableSaveHandler));
        }

        return fieldData;
    }

    private Field[] getFieldPositions(String query, Map<Field, FieldData> fieldData) {
        List<Field> indexes = Lists.newArrayList();

        for (String s : query.split(" ")) {
            for (Map.Entry<Field, FieldData> fieldStringEntry : fieldData.entrySet()) {
                FieldData parameter = fieldStringEntry.getValue();

                if (s.equals(parameter.getName()) || s.startsWith(parameter.getName()) || s.endsWith(parameter.getName()) || s.contains(parameter.getName())) {
                    indexes.add(fieldStringEntry.getKey());
                }
            }
        }

        return indexes.toArray(new Field[0]);
    }

    private BiFunction<EnvyPlayer<?>, Object, PlayerAttribute<?>> getConstructor(Object manager, Class<? extends PlayerAttribute<?>> clazz) {
        try {
            Constructor<? extends PlayerAttribute<?>> constructor = clazz.getConstructor(
                    manager.getClass(),
                    EnvyPlayer.class
            );

            return (envyPlayer, o) -> {
                try {
                    return constructor.newInstance(o, envyPlayer);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                return null;
            };
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String calculateColumnName(Field field) {
        String name = field.getName();
        StringBuilder newName = new StringBuilder();

        for (char c : name.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                newName.append("_");
            } else if(Character.isUpperCase(c)) {
                newName.append("_").append(Character.toLowerCase(c));
            } else {
                newName.append(c);
            }
        }

        return newName.toString();
    }

    public static class AttributeData {

        private final Object manager;
        private final BiFunction<EnvyPlayer<?>, Object, PlayerAttribute<?>> constructor;
        private final Queries queries;
        private final Map<Field, FieldData> fieldData;
        private final Map<String, Field[]> fieldsPositions;

        public AttributeData(Object manager, BiFunction<EnvyPlayer<?>, Object, PlayerAttribute<?>> constructor, Queries queries, Map<Field, FieldData> fieldData, Map<String, Field[]> fieldsPositions) {
            this.manager = manager;
            this.constructor = constructor;
            this.queries = queries;
            this.fieldData = fieldData;
            this.fieldsPositions = fieldsPositions;
        }

        public Object getManager() {
            return this.manager;
        }

        public BiFunction<EnvyPlayer<?>, Object, PlayerAttribute<?>> getConstructor() {
            return this.constructor;
        }

        public Queries getQueries() {
            return this.queries;
        }

        public Map<Field, FieldData> getFieldData() {
            return this.fieldData;
        }

        public Map<String, Field[]> getFieldsPositions() {
            return this.fieldsPositions;
        }
    }

    public static class FieldData {

        private final Field field;
        private final String name;
        private final VariableSaveHandler<?> saveHandler;

        public FieldData(Field field, String name, VariableSaveHandler<?> saveHandler) {
            this.field = field;
            this.name = name;
            this.saveHandler = saveHandler;
        }

        public Field getField() {
            return this.field;
        }

        public String getName() {
            return this.name;
        }

        public VariableSaveHandler<?> getSaveHandler() {
            return this.saveHandler;
        }
    }
}

package com.envyful.api.command.sender;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SenderTypeFactory {

    private static final Map<Class<?>, SenderType<?, ?>> REGISTERED_SENDER_TYPES = Maps.newConcurrentMap();

    /**
     *
     * Registers custom sender types
     *
     * @param senderType The sender type instances
     */
    public static void register(SenderType<?, ?>... senderType) {
        for (SenderType<?, ?> type : senderType) {
            register(type);
        }
    }

    /**
     *
     * Registers a custom sender type
     *
     * @param senderType The sender type instance
     * @return The unmodified sender type
     * @param <T> The generic type
     */
    public static <T extends SenderType<?, ?>> T register(T senderType) {
        REGISTERED_SENDER_TYPES.put(senderType.getType(), senderType);
        return senderType;
    }

    /**
     *
     * Gets a sender type instance from the class
     *
     * @param clazz The class
     * @return The registered sender type
     * @param <A> The generic A
     * @param <B> The generic B
     * @param <T> The sender type generic
     */
    @SuppressWarnings("unchecked")
    public static <A, B, T extends SenderType<A, B>> Optional<T> getSenderType(Class<?> clazz) {
        return (Optional<T>) Optional.ofNullable(REGISTERED_SENDER_TYPES.get(clazz));
    }

    /**
     *
     * Gets all the registered types
     *
     * @return All registered types in a new list
     */
    public static List<SenderType<?, ?>> getAllRegisteredTypes() {
        return Lists.newArrayList(REGISTERED_SENDER_TYPES.values());
    }
}

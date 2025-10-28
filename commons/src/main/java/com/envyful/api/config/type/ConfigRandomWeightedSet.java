package com.envyful.api.config.type;

import com.envyful.api.math.RandomWeightedSet;
import com.google.common.collect.Maps;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

@ConfigSerializable
public class ConfigRandomWeightedSet<A> {

    private Map<String, WeightedObject<A>> entries;
    private transient RandomWeightedSet<A> weightedSet = null;

    public ConfigRandomWeightedSet(Map<String, WeightedObject<A>> entries) {
        this.entries = entries;
    }

    public ConfigRandomWeightedSet(WeightedObject<A>... entries) {
        this.entries = Maps.newHashMap();

        for (WeightedObject<A> entry : entries) {
            this.entries.put(String.valueOf(this.entries.size()), entry);
        }
    }

    public ConfigRandomWeightedSet() {
    }

    public RandomWeightedSet<A> getWeightedSet() {
        if (this.weightedSet == null) {
            this.weightedSet = new RandomWeightedSet<>();

            for (WeightedObject<A> value : this.entries.values()) {
                this.weightedSet.add(value.getObject(), value.weight);
            }
        }

        return this.weightedSet;
    }

    public A getRandom() {
        return this.getWeightedSet().getRandom();
    }

    @ConfigSerializable
    public static class WeightedObject<A> {

        protected double weight;
        protected A object;

        public WeightedObject(double weight, A object) {
            this.weight = weight;
            this.object = object;
        }

        public WeightedObject() {
        }

        public double getWeight() {
            return this.weight;
        }

        public A getObject() {
            return this.object;
        }
    }
}

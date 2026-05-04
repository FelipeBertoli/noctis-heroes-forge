package com.noctisheroes.common.config;

public class EntityConfig {

    public final int xpReward;
    public final int skinCount;

    private EntityConfig(Builder builder) {
        this.xpReward = builder.xpReward;
        this.skinCount = builder.skinCount;
    }

    public static class Builder {
        private int xpReward = 5;
        private int skinCount = 1;

        public Builder xpReward(int v) { this.xpReward = v; return this; }
        public Builder skinCount(int v) { this.skinCount = v; return this; }

        public EntityConfig build() {
            return new EntityConfig(this);
        }
    }
}
package com.noctisheroes.common.config;

public class EntityConfig {

    public final int xpReward;
    public final int skinCount;
    public final float damageThreshold;

    private EntityConfig(Builder builder) {
        this.xpReward = builder.xpReward;
        this.skinCount = builder.skinCount;
        this.damageThreshold = builder.damageThreshold;
    }

    public static class Builder {
        private int xpReward = 5;
        private int skinCount = 1;
        private float damageThreshold = 0.4f;


        public Builder xpReward(int v) { this.xpReward = v; return this; }
        public Builder skinCount(int v) { this.skinCount = v; return this; }
        public Builder damageThreshold(float v) {
            this.damageThreshold = v;
            return this;
        }

        public EntityConfig build() {
            return new EntityConfig(this);
        }
    }
}
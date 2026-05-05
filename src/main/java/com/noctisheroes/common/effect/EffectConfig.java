package com.noctisheroes.common.effect;

public class EffectConfig {

    public final EffectType type;
    public final int duration;
    public final int amplifier;
    public final int tickInterval;
    public final boolean stackable;

    private EffectConfig(Builder builder) {
        this.type = builder.type;
        this.duration = builder.duration;
        this.amplifier = builder.amplifier;
        this.tickInterval = builder.tickInterval;
        this.stackable = builder.stackable;
    }

    public static class Builder {

        private EffectType type;
        private int duration = 100;
        private int amplifier = 0;
        private int tickInterval = 20;
        private boolean stackable = false;

        public Builder type(EffectType type) {
            this.type = type;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder amplifier(int amplifier) {
            this.amplifier = amplifier;
            return this;
        }

        public Builder tickInterval(int tickInterval) {
            this.tickInterval = tickInterval;
            return this;
        }

        public Builder stackable(boolean stackable) {
            this.stackable = stackable;
            return this;
        }

        public EffectConfig build() {
            return new EffectConfig(this);
        }
    }
}
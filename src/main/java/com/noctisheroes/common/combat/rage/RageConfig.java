package com.noctisheroes.common.combat.rage;

public class RageConfig {

    public final float max;
    public final float passiveGain;
    public final float damageMultiplier;

    public RageConfig(Builder builder) {
        this.max = builder.max;
        this.passiveGain = builder.passiveGain;
        this.damageMultiplier = builder.damageMultiplier;
    }

    public static class Builder {
        private float max = 100f;
        private float passiveGain = 0.02f;
        private float damageMultiplier = 0.5f;

        public Builder max(float v) { this.max = v; return this; }
        public Builder passiveGain(float v) { this.passiveGain = v; return this; }
        public Builder damageMultiplier(float v) { this.damageMultiplier = v; return this; }

        public RageConfig build() {
            return new RageConfig(this);
        }
    }
}
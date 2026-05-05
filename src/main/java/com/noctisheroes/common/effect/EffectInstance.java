package com.noctisheroes.common.effect;

import com.noctisheroes.entity.NoctisEntity;

public class EffectInstance {

    final EffectConfig config;
    private int remainingTicks;

    private int tickCounter = 0;

    public EffectInstance(EffectConfig config) {
        this.config = config;
        this.remainingTicks = config.duration;
    }

    public EffectType getType() {
        return config.type;
    }

    public boolean tick(NoctisEntity entity) {

        tickCounter++;
        remainingTicks--;

        if (remainingTicks <= 0) {
            return false;
        }

        if (tickCounter % config.tickInterval == 0) {
            applyEffect(entity);
        }

        return true;
    }

    private void applyEffect(NoctisEntity entity) {

        switch (config.type) {

            case REGEN -> {
                entity.heal(1.0f + config.amplifier);
            }

            case POISON -> {
                entity.hurt(entity.damageSources().magic(), 1.0f + config.amplifier);
            }

            case BURN -> {
                entity.setSecondsOnFire(2 + config.amplifier);
            }

            case SLOW -> {
                entity.setDeltaMovement(entity.getDeltaMovement().scale(0.7));
            }

            case STUN -> {
                entity.setDeltaMovement(0, 0, 0);
            }
        }
    }
}
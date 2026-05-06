package com.noctisheroes.common.combat.rage;

import com.noctisheroes.entity.NoctisEntity;

public class RageHelper {

    public static float getPercent(NoctisEntity entity) {
        if (entity instanceof IRageUser rageUser) {
            return rageUser.getRage().getPercent();
        }
        return 0f;
    }

    public static float getValue(NoctisEntity entity) {
        if (entity instanceof IRageUser rageUser) {
            return rageUser.getRage().getValue();
        }
        return 0f;
    }

    public static void consume(NoctisEntity entity, float amount) {
        if (entity instanceof IRageUser rageUser) {
            rageUser.getRage().consume(amount);
        }
    }
}
package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.animation.AnimationKey;

public interface iNoctisAbility<T extends NoctisEntity> {

    String getId();

    int getCooldown();

    boolean canUse(T entity);

    void start(T entity);

    void tick(T entity);

    void stop(T entity);

    boolean isFinished(T entity);

    boolean overridesAttackAnimation();

    default int getPriority() {
        return 0;
    }

}

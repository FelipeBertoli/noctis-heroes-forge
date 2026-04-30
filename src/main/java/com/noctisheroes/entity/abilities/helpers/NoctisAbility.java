package com.noctisheroes.entity.abilities.helpers;

import com.noctisheroes.entity.base.NoctisEntity;
import software.bernie.geckolib.core.animation.RawAnimation;

public interface NoctisAbility<T extends NoctisEntity> {

    String getId();

    int getCooldown();

    boolean canUse(T entity);

    void start(T entity);

    void tick(T entity);

    void stop(T entity);

    boolean isFinished(T entity);

    RawAnimation getAnimation(); // opcional

    boolean overridesAttackAnimation();
}

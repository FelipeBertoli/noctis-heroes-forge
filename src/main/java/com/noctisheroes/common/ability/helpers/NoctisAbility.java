package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;

public interface NoctisAbility<T extends NoctisEntity> {

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

    default boolean locksAbilities() {
        return false;
    }

    default boolean preventsAttacking() {
        return false;
    }

    default boolean preventsMovement() {
        return false;
    }

    default boolean grantsInvulnerability() { return false;}
}

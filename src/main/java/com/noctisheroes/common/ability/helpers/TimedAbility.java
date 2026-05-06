package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;

public abstract class TimedAbility<T extends NoctisEntity> implements iNoctisAbility<T> {

    protected int ticks;

    protected abstract void onStart(T entity);
    protected abstract void onTick(T entity, int ticks);
    protected abstract void onStop(T entity);

    protected abstract int getDuration();

    @Override
    public void start(T entity) {
        ticks = 0;
        onStart(entity);
    }

    @Override
    public void tick(T entity) {
        ticks++;
        onTick(entity, ticks);

        if (ticks >= getDuration()) {
            stop(entity);
        }
    }

    @Override
    public void stop(T entity) {
        onStop(entity);
    }

    @Override
    public boolean isFinished(T entity) {
        return ticks >= getDuration();
    }
}
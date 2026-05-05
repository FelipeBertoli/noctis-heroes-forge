package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;

public interface IResourceAbility<T extends NoctisEntity> {

    boolean hasResource(T entity);

    void consumeResource(T entity);
}
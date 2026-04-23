package com.noctisheroes.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class AbstractViltrumite extends AbstractMob {
    public AbstractViltrumite(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
}

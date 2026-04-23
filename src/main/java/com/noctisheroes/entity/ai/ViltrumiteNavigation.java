package com.noctisheroes.entity.ai;

import com.noctisheroes.entity.base.AbstractViltrumite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;

public class ViltrumiteNavigation extends FlyingPathNavigation {

    public ViltrumiteNavigation(AbstractViltrumite entity, Level level) {
        super(entity, level);
        // Permite passar por ar livremente
        this.nodeEvaluator.setCanOpenDoors(false);
        this.nodeEvaluator.setCanPassDoors(true);
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        // Voadores não precisam de bloco sólido embaixo
        return !this.level.getBlockState(pos).isAir();
    }
}
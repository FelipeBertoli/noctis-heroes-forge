package com.noctisheroes.entity.base.goals;

import com.noctisheroes.entity.base.AbstractViltrumite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FlyingChaseGoal extends Goal {

    private final AbstractViltrumite mob;
    private LivingEntity target;

    public FlyingChaseGoal(AbstractViltrumite mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = mob.getTarget();
        return this.target != null && mob.isFlying();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && mob.isFlying();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (target == null) return;

        double dx = target.getX() - mob.getX();
        double dy = (target.getY() + target.getBbHeight() * 0.5) - mob.getY();
        double dz = target.getZ() - mob.getZ();

        double speed = 0.02;

        mob.setDeltaMovement(
                dx * speed,
                dy * speed,
                dz * speed
        );
    }
}
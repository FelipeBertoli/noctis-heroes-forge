package com.noctisheroes.entity.base.goals;

import com.noctisheroes.entity.base.AbstractViltrumite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class AerialOrbitGoal extends Goal {

    private final AbstractViltrumite mob;
    private LivingEntity target;

    private float angle;
    private float radius;
    private float speed;
    private float direction;

    private double heightOffset;

    private int changeTimer;

    public AerialOrbitGoal(AbstractViltrumite mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = mob.getTarget();
        return target != null && target.isAlive() && mob.isFlying();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        angle = mob.getRandom().nextFloat() * 360f;
        radius = 4f + mob.getRandom().nextFloat() * 6f;
        speed = 1.5f + mob.getRandom().nextFloat() * 2f;
        direction = mob.getRandom().nextBoolean() ? 1f : -1f;

        heightOffset = 2 + mob.getRandom().nextDouble() * 3;

        changeTimer = 40 + mob.getRandom().nextInt(60);
    }

    @Override
    public void tick() {
        if (target == null) return;

        // 🔥 muda comportamento periodicamente
        if (--changeTimer <= 0) {
            radius = 4f + mob.getRandom().nextFloat() * 6f;
            speed = 1f + mob.getRandom().nextFloat() * 2.5f;

            // chance de inverter direção
            if (mob.getRandom().nextFloat() < 0.3f) {
                direction *= -1;
            }

            heightOffset = 2 + mob.getRandom().nextDouble() * 4;

            changeTimer = 40 + mob.getRandom().nextInt(80);
        }

        // 🔥 avança ângulo
        angle += speed * direction;

        double rad = Math.toRadians(angle);

        // 🔥 posição orbital com irregularidade
        double offsetX = Math.cos(rad) * radius;
        double offsetZ = Math.sin(rad) * radius;

        // 🔥 variação vertical estilo Phantom
        double targetY = target.getY() + heightOffset
                + Math.sin(rad * 2) * 1.2;

        double targetX = target.getX() + offsetX;
        double targetZ = target.getZ() + offsetZ;

        double dx = targetX - mob.getX();
        double dy = targetY - mob.getY();
        double dz = targetZ - mob.getZ();

        double moveSpeed = 0.08;

        mob.setDeltaMovement(
                dx * moveSpeed,
                dy * moveSpeed,
                dz * moveSpeed
        );

        // 🔥 sempre olhando pro player
        mob.getLookControl().setLookAt(target, 30f, 30f);
    }
}
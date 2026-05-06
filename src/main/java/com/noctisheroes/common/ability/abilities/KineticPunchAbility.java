package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.IResourceAbility;
import com.noctisheroes.common.combat.rage.RageHelper;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KineticPunchAbility extends SuperPunchAbility
        implements IResourceAbility<NoctisEntity> {

    private static final float MIN_RAGE = 20f;
    private static final float COST = 15f;

    @Override
    public String getId() {
        return "kinetic_punch";
    }

    @Override
    public int getCooldown() {
        return 40;
    }

    @Override
    public int getPriority() {
        return 35;
    }

    @Override
    protected void performPunch(NoctisEntity entity, LivingEntity target) {

        super.performPunch(entity, target);

        float rage = RageHelper.getPercent(entity);

        Vec3 dir = target.position().subtract(entity.position()).normalize();

        target.setDeltaMovement(
                target.getDeltaMovement().x + dir.x * rage * 2.0,
                target.getDeltaMovement().y + rage * 0.3,
                target.getDeltaMovement().z + dir.z * rage * 2.0
        );
    }

    // =============================
    // 🔥 RESOURCE SYSTEM
    // =============================

    @Override
    public boolean hasResource(NoctisEntity entity) {
        return RageHelper.getValue(entity) >= MIN_RAGE;
    }

    @Override
    public void consumeResource(NoctisEntity entity) {
        RageHelper.consume(entity, COST);
    }

    @Override
    protected void triggerFinalImpact(NoctisEntity entity, LivingEntity target, Level level) {

        float scale = 1f + RageHelper.getPercent(entity);

        level.explode(
                entity,
                target.getX(),
                target.getY(),
                target.getZ(),
                2.5f * scale,
                Level.ExplosionInteraction.MOB
        );
    }

    @Override
    protected float getDamage(NoctisEntity entity) {
        return super.getDamage(entity) * (1f + RageHelper.getPercent(entity));
    }
}

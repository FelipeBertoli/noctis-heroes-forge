package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.AbilityHelper;
import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.common.particle.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SuperPunchAbility extends TimedAbility<NoctisEntity> {

    private LivingEntity hitTarget;
    private boolean impactTriggered;
    private Vec3 lastTargetPos;
    private Vec3 punchDirection;
    private static final int HIT_FRAME = 4;

    private static final int ABILITY_DURATION = 25;
    private static final int ABILITY_COOLDOWN = 100;
    private static final int ABILITY_PRIORITY = 35;
    private static final String ABILITY_ID = "super_punch";

    /**
     * Força total do impulso.
     */
    private static final double PUNCH_FORCE = 1.6;

    /**
     * Quanto a direção vertical influencia.
     *
     * 0.5 = mais horizontal
     * 1.0 = equilibrado
     * 2.0 = muito vertical
     */
    private static final double VERTICAL_INFLUENCE = 1.0;

    /**
     * Limite vertical para evitar ângulos absurdos.
     */
    private static final double MAX_VERTICAL = 0.7;
    private static final float DAMAGE_MULTIPLIER = 1.2f;
    private static final float FINAL_EXPLOSION_RADIUS = 2.5f;
    private static final float DESTRUCTION_RADIUS = 3.0f;

    @Override
    public int getCooldown() { return ABILITY_COOLDOWN;}

    @Override
    public int getPriority() { return ABILITY_PRIORITY; }

    @Override
    protected int getDuration() { return ABILITY_DURATION;}

    @Override
    public String getId() { return ABILITY_ID; }

    @Override
    public boolean canUse(NoctisEntity entity) {
        LivingEntity target = entity.getTarget();
        if (target == null) return false;
        double dist = entity.distanceTo(target);
        return dist > 2 && dist < 8;
            //&& entity.getRandom().nextFloat() < 0.12f;
    }

    @Override
    public boolean overridesAttackAnimation() {
        return true;
    }

    @Override
    protected void onStart(NoctisEntity entity) {
        hitTarget = null;
        impactTriggered = false;
        lastTargetPos = null;
        punchDirection = null;
        entity.setNoGravity(true);
        entity.triggerAnim(
                "actions",
                entity.getRandom().nextBoolean()
                        ? "right_attack"
                        : "left_attack"
        );
    }

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {
        Level level = entity.level();
        LivingEntity target = entity.getTarget();

        if (target == null) {
            stop(entity);
            return;
        }

        if (ticks == HIT_FRAME) performPunch(entity, target);

        if (hitTarget != null) {
            forceTargetMovement(hitTarget);
            if (lastTargetPos != null) AbilityHelper.destroyPath(level, lastTargetPos, hitTarget.position(), DESTRUCTION_RADIUS);

            Vec3 ahead = hitTarget.position().add(punchDirection.scale(1.5));

            AbilityHelper.destroySphere(level, ahead, 3);

            lastTargetPos = hitTarget.position();

            if (!impactTriggered && hasHitSurface(level, hitTarget)) triggerFinalImpact(entity, hitTarget, level);
        }
    }

    @Override
    protected void onStop(NoctisEntity entity) {
        entity.setNoGravity(false);
        if (hitTarget != null) hitTarget.fallDistance = 0;
    }

    protected void performPunch(NoctisEntity entity, LivingEntity target) {
        float damage = getDamage(entity);
        target.hurt(entity.damageSources().mobAttack(entity), damage);

        hitTarget = target;
        lastTargetPos = target.position();
        punchDirection = target.position().subtract(entity.position()).normalize();

        applyLaunchVelocity(target);

        AbilityParticleEffects.spawnCriticalHitEffect(entity.level(), target.position());
    }

    private void forceTargetMovement(LivingEntity target) {
        if (punchDirection == null) return;

        applyLaunchVelocity(target);

        target.hurtMarked = true;
        target.hasImpulse = true;
        target.fallDistance = 0;
    }

    private void applyLaunchVelocity(LivingEntity target) {

        double vertical =
                Mth.clamp(
                        punchDirection.y
                                * VERTICAL_INFLUENCE,
                        -MAX_VERTICAL,
                        MAX_VERTICAL
                );

        Vec3 launch =
                new Vec3(
                        punchDirection.x,
                        vertical,
                        punchDirection.z
                ).normalize()
                        .scale(PUNCH_FORCE);

        target.setDeltaMovement(launch);
        target.hurtMarked = true;
        target.hasImpulse = true;
    }

    protected void triggerFinalImpact(
            NoctisEntity entity,
            LivingEntity target,
            Level level
    ) {

        impactTriggered = true;

        Vec3 pos = target.position();

        AbilityParticleEffects.spawnImpactEffect(level, pos, 1.5);

        AbilityHelper.explode(level, entity, pos, FINAL_EXPLOSION_RADIUS);
    }

    private boolean hasHitSurface(Level level, LivingEntity entity) {
        return ImpactDetector.hasNearbyCollision(level, entity.position(), 2.0);
    }

    protected float getDamage(NoctisEntity entity) {
        return AbilityHelper.scaledAttack(entity, DAMAGE_MULTIPLIER);
    }
}
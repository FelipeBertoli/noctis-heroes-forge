package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.AbilityHelper;
import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.common.particle.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.animation.AnimationKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DestructiveDashAbility extends TimedAbility<NoctisEntity> {

    private LivingEntity hitTarget;
    private boolean impactTriggered;
    private Vec3 dashStartPosition;

    private static final int DASH_DURATION = 5;
    private static final int HIT_FRAME = 6;

    private static final double DASH_SPEED = 2.8;
    private static final float DAMAGE_MULTIPLIER = 1.5f;
    private static final double KNOCKBACK_STRENGTH = 4.5;
    private static final float EXPLOSION_RADIUS = 2.0f;
    private static final double HIT_DETECTION_RANGE = 3.5;
    private static final double IMPACT_DETECTION_RADIUS = 3.0;
    private static final double ABILITY_DURATION = 25;

    @Override
    public String getId() {
        return "destructive_dash";
    }

    @Override
    public int getCooldown() {
        return 100;
    }

    @Override
    public int getPriority() {
        return 10;
    }


    @Override
    protected int getDuration() {
        return 25;
    }

    @Override
    public boolean canUse(NoctisEntity entity) {
        var target = entity.getTarget();
        if (target == null) return false;

        double dist = entity.distanceTo(target);
        return dist > 5.0 && dist < 18.0 && entity.getRandom().nextFloat() < 0.03f;
    }

    @Override
    protected void onStart(NoctisEntity entity) {
        hitTarget = null;
        impactTriggered = false;
        dashStartPosition = entity.position();
        entity.setNoGravity(true);
    }

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {

        Level level = entity.level();
        LivingEntity target = entity.getTarget();

        if (target == null) {
            stop(entity);
            return;
        }

        if (ticks <= DASH_DURATION) {
            AbilityHelper.dashToTarget(entity, target, DASH_SPEED);
        }

        else if (ticks == HIT_FRAME) {
            float damage = AbilityHelper.scaledAttack(entity, 1.5f);
            target.hurt(entity.damageSources().mobAttack(entity), damage);
            hitTarget = target;

            Vec3 knockbackDir = target.position().subtract(entity.position()).normalize();
            target.setDeltaMovement(knockbackDir.x * KNOCKBACK_STRENGTH, 0.6, knockbackDir.z * KNOCKBACK_STRENGTH);
            AbilityParticleEffects.spawnDirectionalSonicBoom(level, entity.position().add(0, entity.getBbHeight() * 0.7, 0), target.position(), 1.0);
            AbilityParticleEffects.spawnCriticalHitEffect(level, target.position().add(0, 1, 0));
        }

        else if (ticks > HIT_FRAME) {
            handleBlockImpactTracking(entity, target, level);
        }
    }

    @Override
    public void onStop(NoctisEntity entity) {
        entity.setNoGravity(false);
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
    }

    @Override
    public boolean isFinished(NoctisEntity entity) {
        return ticks > ABILITY_DURATION;
    }


    private void handleBlockImpactTracking(NoctisEntity entity, LivingEntity target, Level level) {
        if (impactTriggered) {
            return;
        }

        if (ticks < HIT_FRAME + 2) {
            return;
        }

        boolean hasNearbyBlock = ImpactDetector.hasNearbyCollision(
                level,
                target.position(),
                IMPACT_DETECTION_RADIUS
        );

        if (hasNearbyBlock) {
            triggerBlockImpactEffect(entity, target, level);
        }
    }

    private void triggerBlockImpactEffect(NoctisEntity entity, LivingEntity target, Level level) {
        impactTriggered = true;
        Vec3 impactPos = target.position();
        AbilityParticleEffects.spawnImpactEffect(level, impactPos, 1.0);
        AbilityHelper.explode( entity.level(), entity, target.position(), 1.2f);
    }

    public AnimationKey getAnimationKey() {
        return AnimationKey.RIGHT_ATTACK;
    }

    @Override
    public boolean overridesAttackAnimation() {
        return true;
    }


}
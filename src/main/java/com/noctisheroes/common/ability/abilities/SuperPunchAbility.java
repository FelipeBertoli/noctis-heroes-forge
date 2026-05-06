package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.AbilityHelper;
import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.iNoctisAbility;
import com.noctisheroes.common.particle.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * Super Punch com destruição contínua + impacto final.
 */
public class SuperPunchAbility implements iNoctisAbility<NoctisEntity> {

    // =============================
    // 📊 ESTADO
    // =============================

    private int ticks;
    private LivingEntity hitTarget;
    private boolean impactTriggered;
    private Vec3 lastTargetPos;

    // =============================
    // ⚙️ CONFIG
    // =============================

    private static final int HIT_FRAME = 4;
    private static final int ABILITY_DURATION = 25;

    private static final double PUNCH_SPEED = 3.5;
    private static final double PUNCH_VERTICAL = 0.9;

    private static final float DAMAGE_MULTIPLIER = 1.2f;
    private static final float FINAL_EXPLOSION_RADIUS = 2.5f;
    private static final float DESTRUCTION_RADIUS = 3.0f;

    // =============================
    // 🎯 ABILITY
    // =============================

    @Override
    public int getCooldown() {
        return 30;
    }

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public String getId() {
        return "super_punch";
    }

    @Override
    public boolean canUse(NoctisEntity entity) {
        var target = entity.getTarget();
        if (target == null) return false;

        double dist = entity.distanceTo(target);
        return dist > 2 && dist < 8 && entity.getRandom().nextFloat() < 0.04f;
    }

    @Override
    public void start(NoctisEntity entity) {
        ticks = 0;
        hitTarget = null;
        impactTriggered = false;
        lastTargetPos = null;

        entity.setNoGravity(true);
    }

    @Override
    public void tick(NoctisEntity entity) {
        ticks++;

        Level level = entity.level();
        LivingEntity target = entity.getTarget();

        if (target == null) {
            stop(entity);
            return;
        }

        // =============================
        // 🥊 FRAME DO SOCO
        // =============================
        if (ticks == HIT_FRAME) {
            performPunch(entity, target);
        }

        // =============================
        // 💥 DESTRUIÇÃO CONTÍNUA + IMPACTO
        // =============================
        if (hitTarget != null) {

            // 🔥 destruição ao longo do caminho
            if (lastTargetPos != null) {
                AbilityHelper.destroyPath(
                        level,
                        lastTargetPos,
                        hitTarget.position(),
                        DESTRUCTION_RADIUS
                );
            }

            lastTargetPos = hitTarget.position();

            // 💥 impacto final
            if (!impactTriggered && hasHitSurface(level, hitTarget)) {
                triggerFinalImpact(entity, hitTarget, level);
            }
        }

        if (ticks >= ABILITY_DURATION) {
            stop(entity);
        }
    }

    // =============================
    // 🥊 EXECUÇÃO DO SOCO
    // =============================

    protected void performPunch(NoctisEntity entity, LivingEntity target) {

        float damage = getDamage(entity);

        target.hurt(entity.damageSources().mobAttack(entity), damage);

        hitTarget = target;
        lastTargetPos = target.position();

        Vec3 direction = target.position().subtract(entity.position()).normalize();

        AbilityHelper.applyKnockback(
                target,
                direction,
                PUNCH_SPEED,
                PUNCH_VERTICAL
        );

        AbilityParticleEffects.spawnCriticalHitEffect(entity.level(), target.position());
    }

    // =============================
    // 💥 IMPACTO FINAL
    // =============================

    protected void triggerFinalImpact(NoctisEntity entity, LivingEntity target, Level level) {
        impactTriggered = true;

        Vec3 pos = target.position();

        AbilityParticleEffects.spawnImpactEffect(level, pos, 1.5);

        AbilityHelper.explode(
                level,
                entity,
                pos,
                FINAL_EXPLOSION_RADIUS
        );
    }

    // =============================
    // 🧠 DETECÇÃO DE SUPERFÍCIE
    // =============================

    private boolean hasHitSurface(Level level, LivingEntity entity) {
        return ImpactDetector.hasNearbyCollision(
                level,
                entity.position(),
                2.0
        );
    }

    // =============================
    // 🏁 FINALIZAÇÃO
    // =============================

    @Override
    public void stop(NoctisEntity entity) {
        entity.setNoGravity(false);
    }

    @Override
    public boolean isFinished(NoctisEntity entity) {
        return ticks > ABILITY_DURATION;
    }

    @Override
    public RawAnimation getAnimation() {
        return NoctisEntity.RIGHT_ATTACK_ANIM;
    }

    @Override
    public boolean overridesAttackAnimation() {
        return true;
    }

    protected float getDamage(NoctisEntity entity) {
        return AbilityHelper.scaledAttack(entity, DAMAGE_MULTIPLIER);
    }
}
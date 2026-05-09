package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.AbilityHelper;
import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.common.particle.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * Super Punch com TimedAbility.
 */
public class SuperPunchAbility extends TimedAbility<NoctisEntity> {

    // =============================
    // 📊 ESTADO
    // =============================

    private LivingEntity hitTarget;
    private boolean impactTriggered;
    private Vec3 lastTargetPos;
    private Vec3 punchDirection;

    // =============================
    // ⚙️ CONFIG
    // =============================

    private static final int HIT_FRAME = 4;

    private static final double PUNCH_SPEED = 2.2;
    private static final double PUNCH_VERTICAL = 0.9;

    private static final float DAMAGE_MULTIPLIER = 1.2f;

    private static final float FINAL_EXPLOSION_RADIUS = 2.5f;

    private static final float DESTRUCTION_RADIUS = 4.0f;

    // =============================
    // 🎯 METADADOS
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

        if (target == null) {
            return false;
        }

        double dist = entity.distanceTo(target);

        return dist > 2
                && dist < 8
                && entity.getRandom().nextFloat() < 0.04f;
    }

    @Override
    public RawAnimation getAnimation() {
        return NoctisEntity.RIGHT_ATTACK_ANIM;
    }

    @Override
    public boolean overridesAttackAnimation() {
        return true;
    }

    // =============================
    // 🚀 START
    // =============================

    @Override
    protected void onStart(NoctisEntity entity) {

        hitTarget = null;

        impactTriggered = false;

        lastTargetPos = null;

        punchDirection = null;

        entity.setNoGravity(true);
    }

    // =============================
    // 🔄 TICK
    // =============================

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {

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
        // 💥 MOVIMENTO FORÇADO
        // =============================

        if (hitTarget != null) {

            forceTargetMovement(hitTarget);

            // 🔥 destruição do caminho
            if (lastTargetPos != null) {

                AbilityHelper.destroyPath(
                        level,
                        lastTargetPos,
                        hitTarget.position(),
                        DESTRUCTION_RADIUS
                );
            }

            // 🔥 destrói na frente do alvo
            Vec3 ahead = hitTarget.position()
                    .add(punchDirection.scale(1.5));

            AbilityHelper.destroySphere(
                    level,
                    ahead,
                    3
            );

            lastTargetPos = hitTarget.position();

            // =============================
            // 💥 IMPACTO FINAL
            // =============================

            if (!impactTriggered
                    && hasHitSurface(level, hitTarget)) {

                triggerFinalImpact(
                        entity,
                        hitTarget,
                        level
                );
            }
        }
    }

    // =============================
    // 🏁 STOP
    // =============================

    @Override
    protected void onStop(NoctisEntity entity) {

        entity.setNoGravity(false);

        if (hitTarget != null) {

            hitTarget.fallDistance = 0;
        }
    }

    // =============================
    // ⏳ DURAÇÃO
    // =============================

    @Override
    protected int getDuration() {
        return 25;
    }

    // =============================
    // 🥊 EXECUÇÃO DO SOCO
    // =============================

    protected void performPunch(
            NoctisEntity entity,
            LivingEntity target
    ) {

        float damage = getDamage(entity);

        target.hurt(
                entity.damageSources().mobAttack(entity),
                damage
        );

        hitTarget = target;

        lastTargetPos = target.position();

        punchDirection =
                target.position()
                        .subtract(entity.position())
                        .normalize();

        // 🔥 impulso inicial
        target.setDeltaMovement(
                punchDirection.x * PUNCH_SPEED,
                PUNCH_VERTICAL,
                punchDirection.z * PUNCH_SPEED
        );

        target.hurtMarked = true;
        target.hasImpulse = true;

        entity.level().players().forEach(player -> {

            player.displayClientMessage(
                    Component.literal("INICIANDO SUPER SOCO"),
                    false
            );
        });

        AbilityParticleEffects.spawnCriticalHitEffect(
                entity.level(),
                target.position()
        );
    }

    // =============================
    // 🚀 MOVIMENTO FORÇADO
    // =============================

    private void forceTargetMovement(
            LivingEntity target
    ) {

        if (punchDirection == null) {
            return;
        }

        target.setDeltaMovement(
                punchDirection.x * PUNCH_SPEED,
                PUNCH_VERTICAL,
                punchDirection.z * PUNCH_SPEED
        );

        target.hurtMarked = true;

        target.hasImpulse = true;

        target.fallDistance = 0;
    }

    // =============================
    // 💥 IMPACTO FINAL
    // =============================

    protected void triggerFinalImpact(
            NoctisEntity entity,
            LivingEntity target,
            Level level
    ) {

        impactTriggered = true;

        Vec3 pos = target.position();

        AbilityParticleEffects.spawnImpactEffect(
                level,
                pos,
                1.5
        );

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

    private boolean hasHitSurface(
            Level level,
            LivingEntity entity
    ) {

        return ImpactDetector.hasNearbyCollision(
                level,
                entity.position(),
                2.0
        );
    }

    // =============================
    // ⚔️ DANO
    // =============================

    protected float getDamage(
            NoctisEntity entity
    ) {

        return AbilityHelper.scaledAttack(
                entity,
                DAMAGE_MULTIPLIER
        );
    }
}
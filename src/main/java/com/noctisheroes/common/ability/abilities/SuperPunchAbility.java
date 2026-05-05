package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.NoctisAbility;
import com.noctisheroes.common.ability.helpers.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;

import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * Super Punch com destruição contínua + impacto final.
 */
public class SuperPunchAbility implements NoctisAbility<NoctisEntity> {

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

    private static final float DESTRUCTION_RADIUS = 3.0f; // 🔥 área de destruição contínua

    // =============================
    // 🎯 ABILITY
    // =============================

    @Override
    public String getId() {
        return "super_punch";
    }

    @Override
    public int getCooldown() {
        return 70;
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
        // 💥 DESTRUIÇÃO CONTÍNUA
        // =============================
        if (hitTarget != null) {

            if (lastTargetPos != null) {
                destroyBlocksAlongPath(level, lastTargetPos, hitTarget.position(), DESTRUCTION_RADIUS);
            }

            lastTargetPos = hitTarget.position();

            // Detecta impacto final
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

    private void performPunch(NoctisEntity entity, LivingEntity target) {

        float baseDamage = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = baseDamage * DAMAGE_MULTIPLIER;

        target.hurt(entity.damageSources().mobAttack(entity), damage);

        hitTarget = target;
        lastTargetPos = target.position();

        Vec3 direction = target.position().subtract(entity.position()).normalize();

        target.setDeltaMovement(
                direction.x * PUNCH_SPEED,
                PUNCH_VERTICAL,
                direction.z * PUNCH_SPEED
        );

        AbilityParticleEffects.spawnCriticalHitEffect(entity.level(), target.position());
    }

    // =============================
    // 💥 IMPACTO FINAL
    // =============================

    private void triggerFinalImpact(NoctisEntity entity, LivingEntity target, Level level) {
        impactTriggered = true;

        Vec3 pos = target.position();

        // 🔥 apenas visual
        AbilityParticleEffects.spawnImpactEffect(level, pos, 1.5);

        // ⚠️ explosão leve (ou remove se quiser zero dano)
        level.explode(
                entity,
                pos.x,
                pos.y,
                pos.z,
                FINAL_EXPLOSION_RADIUS,
                Level.ExplosionInteraction.MOB
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
    // 💣 DESTRUIÇÃO CONTÍNUA
    // =============================

    private void destroyBlocksAlongPath(Level level, Vec3 start, Vec3 end, float radius) {
        if (!(level instanceof ServerLevel)) return;

        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1) return;

        direction = direction.normalize();

        int steps = (int) (distance * 3); // precisão alta

        for (int i = 0; i < steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = start.add(direction.scale(distance * progress));

            destroySphere(level, pos, radius);
        }
    }

    private void destroySphere(Level level, Vec3 center, float radius) {

        int r = (int) Math.ceil(radius);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {

                    if (x*x + y*y + z*z > radius * radius) continue;

                    BlockPos pos = BlockPos.containing(
                            center.x + x,
                            center.y + y,
                            center.z + z
                    );

                    BlockState state = level.getBlockState(pos);

                    if (state.isAir()) continue;

                    // 🔥 evita quebrar blocos muito resistentes (opcional)
                    if (state.getDestroySpeed(level, pos) > 10f) continue;

                    level.destroyBlock(pos, true);
                }
            }
        }
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
}
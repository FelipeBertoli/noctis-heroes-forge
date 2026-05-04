package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.NoctisAbility;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.core.BlockPos;
import com.noctisheroes.common.ability.helpers.AbilityParticleEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * Ability de Super Soco com impacto explosivo.
 *
 * ✅ Sistema melhorado:
 * - Detecção robusta de impacto
 * - Partículas de quebra de som
 * - Explosão ao atingir superfícies próximas
 * - Knockback controlado
 * - Sistema de fase (preparação, execução, impacto)
 */
public class SuperPunchAbility implements NoctisAbility<NoctisEntity> {

    // =============================
    // 📊 ESTADO
    // =============================

    private int ticks;
    private LivingEntity hitTarget;
    private boolean impactTriggered;
    private Vec3 punchStartPosition;

    // =============================
    // ⚙️ CONFIGURAÇÃO
    // =============================

    private static final int HIT_FRAME = 4;              // Frame do golpe
    private static final int ABILITY_DURATION = 20;      // Duração total
    private static final int IMPACT_CHECK_DELAY = 2;     // Delay para verificar impacto

    private static final double PUNCH_SPEED = 3.5;       // Velocidade do knockback
    private static final double PUNCH_VERTICAL_BOOST = 0.9;
    private static final float DAMAGE_MULTIPLIER = 1.2f;
    private static final float EXPLOSION_RADIUS = 2.5f;
    private static final double IMPACT_DETECTION_RADIUS = 2.5; // Raio de detecção

    private static final double PUNCH_INTENSITY = 1.5;   // Intensidade dos efeitos

    // =============================
    // 🎯 ABILITY INTERFACE
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
        // Usa ability quando está numa faixa específica de distância
        return dist > 2.0 && dist < 8.0 && entity.getRandom().nextFloat() < 0.04f;
    }

    @Override
    public void start(NoctisEntity entity) {
        ticks = 0;
        hitTarget = null;
        impactTriggered = false;
        punchStartPosition = entity.position();

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
        // ⚡ FASE 1: PREPARAÇÃO (0-3 ticks)
        // =============================
        if (ticks < HIT_FRAME) {
            handlePreparationPhase(entity, target);
        }

        // =============================
        // 🥊 FASE 2: EXECUÇÃO DO SOCO (frame 4)
        // =============================
        else if (ticks == HIT_FRAME) {
            handlePunchExecution(entity, target, level);
        }

        // =============================
        // 💥 FASE 3: RASTREAMENTO DE IMPACTO
        // =============================
        else if (ticks > HIT_FRAME) {
            handleImpactTracking(entity, target, level);
        }

        // =============================
        // 🏁 FINALIZAÇÃO
        // =============================
        if (ticks >= ABILITY_DURATION) {
            stop(entity);
        }
    }

    /**
     * Fase 1: Preparação para o soco (animação de carga).
     */
    private void handlePreparationPhase(NoctisEntity entity, LivingEntity target) {
        // O mob se posiciona para atingir o alvo
        Vec3 direction = target.position()
                .subtract(entity.position())
                .normalize();

        // Partículas de preparação (quebra de som suave)
        if (ticks % 2 == 0) {
            AbilityParticleEffects.spawnAirBreakerEffect(
                    entity.level(),
                    entity.position().add(0, entity.getBbHeight() * 0.5, 0),
                    0.5
            );
        }
    }

    /**
     * Fase 2: Execução do soco (frame do golpe).
     */
    private void handlePunchExecution(NoctisEntity entity, LivingEntity target, Level level) {
        // =============================
        // 🎯 APLICAR DANO
        // =============================

        float baseDamage = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = baseDamage * DAMAGE_MULTIPLIER;

        target.hurt(entity.damageSources().mobAttack(entity), finalDamage);

        hitTarget = target;

        // =============================
        // 🚀 APLICAR KNOCKBACK
        // =============================

        Vec3 direction = target.position()
                .subtract(entity.position())
                .normalize();

        target.setDeltaMovement(
                direction.x * PUNCH_SPEED,
                PUNCH_VERTICAL_BOOST,
                direction.z * PUNCH_SPEED
        );

        // =============================
        // ✨ PARTÍCULAS DE SOCO
        // =============================

        Vec3 punchCenter = entity.position().add(
                0,
                entity.getBbHeight() * 0.7,
                0
        );

        // Quebra de som ao longo do caminho
        AbilityParticleEffects.spawnDirectionalSonicBoom(
                level,
                punchCenter,
                target.position(),
                PUNCH_INTENSITY
        );

        // Efeito crítico no ponto de impacto
        AbilityParticleEffects.spawnCriticalHitEffect(level, target.position().add(0, 1, 0));
    }

    /**
     * Fase 3: Rastreamento de impacto com a superfície.
     */
    private void handleImpactTracking(NoctisEntity entity, LivingEntity target, Level level) {
        // Só verifica impacto se o alvo foi atingido
        if (hitTarget == null || impactTriggered) {
            return;
        }

        // Aguarda alguns ticks para o alvo estar em movimento
        if (ticks < HIT_FRAME + IMPACT_CHECK_DELAY) {
            return;
        }

        // =============================
        // 📍 DETECTAR IMPACTO
        // =============================

        boolean hasNearbyBlock = ImpactDetector.hasNearbyCollision(
                level,
                target.position(),
                IMPACT_DETECTION_RADIUS
        );

        if (hasNearbyBlock) {
            triggerImpactEffect(entity, target, level);
        }
    }

    /**
     * Ativa o efeito de impacto (explosão, partículas).
     */
    private void triggerImpactEffect(NoctisEntity entity, LivingEntity target, Level level) {
        impactTriggered = true;

        Vec3 impactPos = target.position();

        // =============================
        // 💥 EFEITOS VISUAIS
        // =============================

        // Partículas de impacto massivas
        AbilityParticleEffects.spawnImpactEffect(level, impactPos, PUNCH_INTENSITY);

        // Explosão de blocos
        if (level instanceof ServerLevel serverLevel) {

            // Encontra o bloco mais próximo para som
            BlockPos impactBlock = ImpactDetector.findNearestImpactBlock(
                    level,
                    impactPos,
                    IMPACT_DETECTION_RADIUS
            );

            // Causa explosão (quebra blocos fracos)
            level.explode(
                    entity,
                    impactPos.x,
                    impactPos.y,
                    impactPos.z,
                    EXPLOSION_RADIUS,
                    Level.ExplosionInteraction.MOB
            );

        }
    }

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
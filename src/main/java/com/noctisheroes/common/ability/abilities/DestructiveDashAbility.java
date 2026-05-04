package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.ImpactDetector;
import com.noctisheroes.common.ability.helpers.NoctisAbility;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.RawAnimation;
import com.noctisheroes.common.ability.helpers.AbilityParticleEffects;

/**
 * Ability de Dash Destrutivo com impacto explosivo.
 *
 * ✅ Melhorias:
 * - Sistema de 3 fases bem definidas
 * - Detecção robusta de impacto
 * - Partículas de rastro durante dash
 * - Knockback controlado
 * - Detecção de colisão com blocos
 * - Performance otimizada
 */
public class DestructiveDashAbility implements NoctisAbility<NoctisEntity> {

    // =============================
    // 📊 ESTADO
    // =============================

    private int ticks;
    private LivingEntity hitTarget;
    private boolean impactTriggered;
    private Vec3 dashStartPosition;

    // =============================
    // ⚙️ CONFIGURAÇÃO
    // =============================

    private static final int DASH_DURATION = 5;          // Ticks para fazer o dash
    private static final int HIT_FRAME = 6;              // Frame do impacto
    private static final int ABILITY_DURATION = 20;      // Duração total

    private static final double DASH_SPEED = 2.8;        // Velocidade do dash
    private static final float DAMAGE_MULTIPLIER = 1.5f;
    private static final double KNOCKBACK_STRENGTH = 4.5;
    private static final float EXPLOSION_RADIUS = 3.0f;
    private static final double HIT_DETECTION_RANGE = 3.5;
    private static final double IMPACT_DETECTION_RADIUS = 3.0;

    private static final double DASH_INTENSITY = 1.0;    // Intensidade dos efeitos

    // =============================
    // 🎯 ABILITY INTERFACE
    // =============================

    @Override
    public String getId() {
        return "destructive_dash";
    }

    @Override
    public int getCooldown() {
        return 100;
    }

    @Override
    public boolean canUse(NoctisEntity entity) {
        var target = entity.getTarget();
        if (target == null) return false;

        double dist = entity.distanceTo(target);
        // Distância maior que super punch para evitar spam
        return dist > 5.0 && dist < 18.0 && entity.getRandom().nextFloat() < 0.03f;
    }

    @Override
    public void start(NoctisEntity entity) {
        ticks = 0;
        hitTarget = null;
        impactTriggered = false;
        dashStartPosition = entity.position();

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
        // ⚡ FASE 1: DASH INICIAL (0-5 ticks)
        // =============================
        if (ticks <= DASH_DURATION) {
            handleDashPhase(entity, target, level);
        }

        // =============================
        // 💥 FASE 2: IMPACTO (frame 6)
        // =============================
        else if (ticks == HIT_FRAME) {
            handleImpactExecution(entity, target, level);
        }

        // =============================
        // 🔍 FASE 3: RASTREAMENTO DE BLOCOS
        // =============================
        else if (ticks > HIT_FRAME) {
            handleBlockImpactTracking(entity, target, level);
        }

        // =============================
        // 🏁 FINALIZAÇÃO
        // =============================
        if (ticks >= ABILITY_DURATION) {
            stop(entity);
        }
    }

    /**
     * Fase 1: Dash inicial em direção ao alvo.
     */
    private void handleDashPhase(NoctisEntity entity, LivingEntity target, Level level) {
        // Calcula direção
        Vec3 direction = target.position()
                .subtract(entity.position())
                .normalize();

        // Aplica movimento
        entity.setDeltaMovement(direction.scale(DASH_SPEED));

        // Spawn partículas de rastro
        if (ticks % 2 == 0) {
            AbilityParticleEffects.spawnSonicBoomEffect(
                    level,
                    entity.position(),
                    0.7
            );
        }
    }

    /**
     * Fase 2: Execução do impacto.
     */
    private void handleImpactExecution(NoctisEntity entity, LivingEntity target, Level level) {
        double distanceToTarget = entity.distanceTo(target);

        // Verifica se está próximo o suficiente para atingir
        if (distanceToTarget > HIT_DETECTION_RANGE) {
            return; // Alvo passou pelo mob, ignora
        }

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

        Vec3 knockbackDir = target.position()
                .subtract(entity.position())
                .normalize();

        target.setDeltaMovement(
                knockbackDir.x * KNOCKBACK_STRENGTH,
                0.6,
                knockbackDir.z * KNOCKBACK_STRENGTH
        );

        // =============================
        // ✨ EFEITOS VISUAIS
        // =============================

        // Sonic boom direcional
        AbilityParticleEffects.spawnDirectionalSonicBoom(
                level,
                entity.position().add(0, entity.getBbHeight() * 0.7, 0),
                target.position(),
                DASH_INTENSITY
        );

        // Efeito crítico
        AbilityParticleEffects.spawnCriticalHitEffect(level, target.position().add(0, 1, 0));
    }

    /**
     * Fase 3: Rastreamento de impacto com blocos.
     */
    private void handleBlockImpactTracking(NoctisEntity entity, LivingEntity target, Level level) {
        // Só faz uma vez
        if (impactTriggered) {
            return;
        }

        // Aguarda alguns ticks
        if (ticks < HIT_FRAME + 2) {
            return;
        }

        // =============================
        // 📍 DETECTAR IMPACTO COM BLOCO
        // =============================

        boolean hasNearbyBlock = ImpactDetector.hasNearbyCollision(
                level,
                target.position(),
                IMPACT_DETECTION_RADIUS
        );

        if (hasNearbyBlock) {
            triggerBlockImpactEffect(entity, target, level);
        }
    }

    /**
     * Ativa efeito de impacto com blocos.
     */
    private void triggerBlockImpactEffect(NoctisEntity entity, LivingEntity target, Level level) {
        impactTriggered = true;

        Vec3 impactPos = target.position();

        // =============================
        // 💥 EFEITOS VISUAIS
        // =============================

        AbilityParticleEffects.spawnImpactEffect(level, impactPos, DASH_INTENSITY);

        // Causa explosão
        level.explode(
                entity,
                impactPos.x,
                impactPos.y,
                impactPos.z,
                EXPLOSION_RADIUS,
                Level.ExplosionInteraction.MOB
        );

    }

    @Override
    public void stop(NoctisEntity entity) {
        entity.setNoGravity(false);
        // Para apenas o movimento horizontal
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
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
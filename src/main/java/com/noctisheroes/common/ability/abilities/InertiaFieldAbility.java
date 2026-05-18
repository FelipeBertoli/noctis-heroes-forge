package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.IResourceAbility;
import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.common.combat.rage.RageHelper;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class InertiaFieldAbility extends TimedAbility<NoctisEntity> implements IResourceAbility<NoctisEntity> {

    private static final int ABILITY_DURATION = 80;
    private static final int ABILITY_COOLDOWN = 280;
    private static final int ABILITY_PRIORITY = 15;
    private static final String ABILITY_ID = "inertia_field";
    private static final float MIN_RAGE = 30f;
    private static final float RAGE_COST = 20f;
    private static final double FIELD_RADIUS = 5.0;
    private static final int EFFECT_DURATION = 40;

    @Override
    public boolean canUse(NoctisEntity entity) {
        LivingEntity target = entity.getTarget();
        if (target == null) return false;
        return entity.distanceTo(target) <= 12;
    }

    @Override
    protected void onStart(NoctisEntity entity) {

    }

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {

        if (!(entity.level() instanceof ServerLevel level)) return;

        spawnAura(level, entity);

        AABB area = entity.getBoundingBox().inflate(FIELD_RADIUS);

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                target ->
                        target != entity &&
                                target.isAlive()
        );

        for (LivingEntity target : entities) {

            double distance = target.distanceTo(entity);

            if (distance > FIELD_RADIUS) continue;

            target.addEffect(
                    new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            EFFECT_DURATION,
                            2,
                            false,
                            true
                    )
            );

            target.addEffect(
                    new MobEffectInstance(
                            MobEffects.WEAKNESS,
                            EFFECT_DURATION,
                            1,
                            false,
                            true
                    )
            );

            target.addEffect(
                    new MobEffectInstance(
                            MobEffects.DIG_SLOWDOWN,
                            EFFECT_DURATION,
                            1,
                            false,
                            true
                    )
            );
        }
    }


    @Override
    protected void onStop(NoctisEntity entity) {

    }

    private void spawnAura(
            ServerLevel level,
            NoctisEntity entity
    ) {

        // =====================================
        // 🌌 ANEL EXTERNO
        // =====================================

        for (int i = 0; i < 40; i++) {

            double angle = (Math.PI * 2) * (i / 40.0);

            double radius = FIELD_RADIUS;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            double y = 0.2 + Math.sin((entity.tickCount + i) * 0.15) * 0.4;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                    entity.getX() + x,
                    entity.getY() + 1 + y,
                    entity.getZ() + z,
                    2,
                    0.05,
                    0.05,
                    0.05,
                    0.02
            );
        }

        // =====================================
        // 🌀 PARTÍCULAS GIRANDO
        // =====================================

        for (int i = 0; i < 20; i++) {

            double time = (entity.tickCount * 0.08) + i;

            double radius = 2.0 + (i * 0.15);

            double x = Math.cos(time) * radius;
            double z = Math.sin(time) * radius;

            double y = (i % 5) * 0.4;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                    entity.getX() + x,
                    entity.getY() + y + 0.5,
                    entity.getZ() + z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }

        // =====================================
        // 💥 NÚCLEO DE ENERGIA
        // =====================================

        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                entity.getX(),
                entity.getY() + 1.0,
                entity.getZ(),
                8,
                0.6,
                0.8,
                0.6,
                0.03
        );

        // =====================================
        // ⚡ EXPLOSÕES ALEATÓRIAS
        // =====================================

        if (entity.tickCount % 4 == 0) {

            double rx = (Math.random() - 0.5) * FIELD_RADIUS * 2;
            double ry = Math.random() * 2.5;
            double rz = (Math.random() - 0.5) * FIELD_RADIUS * 2;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    entity.getX() + rx,
                    entity.getY() + ry,
                    entity.getZ() + rz,
                    4,
                    0.1,
                    0.1,
                    0.1,
                    0.02
            );
        }
    }

    @Override
    public boolean hasResource(NoctisEntity entity) {
        return RageHelper.getValue(entity) >= MIN_RAGE;
    }

    @Override
    public void consumeResource(NoctisEntity entity) {
        RageHelper.consume(entity, RAGE_COST);
    }

    @Override
    public String getId() {
        return ABILITY_ID;
    }

    @Override
    public int getCooldown() {
        return ABILITY_COOLDOWN;
    }

    @Override
    protected int getDuration() {
        return ABILITY_DURATION;
    }

    @Override
    public int getPriority() {
        return ABILITY_PRIORITY;
    }

    @Override
    public boolean overridesAttackAnimation() {
        return true;
    }
}
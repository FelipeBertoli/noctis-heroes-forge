package com.noctisheroes.common.particle;

import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Gerenciador de partículas para efeitos visuais de abilities.
 *
 * ✅ Recursos:
 * - Partículas de quebra de som (sonic boom)
 * - Partículas de impacto
 * - Partículas de rastro de movimento contínuo
 * - Rastro de hunt flight (quebra de som)
 * - Efeitos configuráveis
 */
public class AbilityParticleEffects {

    public static void spawnAirBreakerEffect(Level level, Vec3 position, double intensity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int particleCount = (int) (2 + intensity * 3);
        double radius = 1.2 * intensity;

        // Partículas ao redor (esfera)
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (Math.random() - 0.5) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    position.x + x,
                    position.y + y,
                    position.z + z,
                    1,
                    x * 0.1, y * 0.1, z * 0.1,
                    0.1
            );
        }
    }


    public static void spawnSonicBoomEffect(NoctisEntity entity) {
        if (!(entity.level() instanceof ServerLevel server)) return;

        server.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                entity.getX(),
                entity.getY(0.5),
                entity.getZ(),
                1,
                0, 0, 0,
                0
        );

        entity.level().playSound(
                null,
                entity.blockPosition(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                0.6f,
                1.2f
        );
    }

    public static void spawnKineticBoomEffect(Level level, Vec3 position, double intensity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int particleCount = (int) (8 + intensity * 4);
        double radius = 1.5 * intensity;

        DustParticleOptions purple = new DustParticleOptions(
                new Vector3f(0.443f, 0.0f, 0.592f),
                (float) intensity
        );

        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (Math.random() - 0.5) * radius;

            serverLevel.sendParticles(
                    purple,
                    position.x + x,
                    position.y + y,
                    position.z + z,
                    1,
                    x * 0.1, y * 0.1, z * 0.1,
                    0.05
            );
        }
    }
    /**
     * Rastro contínuo de sonic boom para hunt flight.
     * Deixa um caminho visual de quebra de som.
     */
    public static void spawnHuntFlightTrail(Level level, Vec3 position, Vec3 velocity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Apenas se está se movendo rápido
        double speed = velocity.length();
        if (speed < 0.5) return;

        // Densidade de partículas baseada em velocidade
        int particleCount = Math.min(8, (int) (speed * 4));

        // Partículas em padrão de espiral ao redor do mob
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount;
            double radius = 0.8 + (speed * 0.3);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (Math.sin(angle * 2) * 0.3);

            // Velocidade "atrás" do mob
            double velocityX = -velocity.x * 0.1;
            double velocityY = -velocity.y * 0.05;
            double velocityZ = -velocity.z * 0.1;

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    position.x + x,
                    position.y + y,
                    position.z + z,
                    1,
                    velocityX, velocityY, velocityZ,
                    0.08
            );
        }

    }

    /**
     * Rastro mais intenso para sonic boom de hunt flight.
     * Versão mais agressiva e visual.
     */
    public static void spawnIntensiveHuntTrail(Level level, Vec3 position, Vec3 velocity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double speed = velocity.length();
        if (speed < 0.3) return;

        // =============================
        // CAMADA 1: Rastro envolvente
        // =============================
        int wrapCount = Math.min(12, (int) (speed * 6));
        for (int i = 0; i < wrapCount; i++) {
            double angle = (Math.PI * 2 * i) / wrapCount + (System.currentTimeMillis() % 1000) * 0.01;
            double radius = 1.0 + (speed * 0.2);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(angle * 3) * 0.5;

            serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    position.x + x,
                    position.y + y,
                    position.z + z,
                    1,
                    velocity.x * -0.05, velocity.y * -0.05, velocity.z * -0.05,
                    0.1
            );
        }

        // =============================
        // CAMADA 2: Ondas de choque frontal
        // =============================
        Vec3 direction = velocity.normalize();
        Vec3 wavePos = position.add(direction.scale(1.2));

        int waveCount = (int) (speed * 3);
        for (int i = 0; i < waveCount; i++) {
            double angle = Math.PI * 2 * Math.random();
            double waveRadius = 0.3 + (i * 0.15);

            double x = Math.cos(angle) * waveRadius;
            double z = Math.sin(angle) * waveRadius;

            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    wavePos.x + x,
                    wavePos.y,
                    wavePos.z + z,
                    1,
                    x * 0.2, 0.05, z * 0.2,
                    0.15
            );
        }

        // =============================
        // CAMADA 3: Partículas traseiras
        // =============================
        Vec3 backPos = position.subtract(direction.scale(0.8));
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 2 * Math.random();
            double x = Math.cos(angle) * 0.6;
            double z = Math.sin(angle) * 0.6;

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    backPos.x + x,
                    backPos.y + (Math.random() - 0.5) * 0.5,
                    backPos.z + z,
                    1,
                    velocity.x * -0.15, velocity.y * -0.1, velocity.z * -0.15,
                    0.12
            );
        }
    }
    public static void spawnKineticHuntFlightTrail(Level level, Vec3 position, Vec3 velocity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double speed = velocity.length();
        if (speed < 0.5) return;

        Vec3 direction = velocity.normalize();

        // =============================
        // 🎨 CORES
        // =============================

        // Roxo principal (#710097)
        DustParticleOptions corePurple = new DustParticleOptions(
                new Vector3f(0.443f, 0.0f, 0.592f),
                1.4f
        );

        // Roxo claro (brilho energético)
        DustParticleOptions glowPurple = new DustParticleOptions(
                new Vector3f(0.75f, 0.2f, 1.0f),
                0.8f
        );

        int density = Math.min(12, (int) (speed * 6));

        // =============================
        // 💥 CAMADA 1 — NÚCLEO (FORTE)
        // =============================

        for (int i = 0; i < density; i++) {
            double spread = 0.3;

            serverLevel.sendParticles(
                    corePurple,
                    position.x,
                    position.y + 0.5,
                    position.z,
                    1,
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread,
                    0.02
            );
        }

        // =============================
        // 🌀 CAMADA 2 — ESPIRAL CINÉTICA
        // =============================

        for (int i = 0; i < density; i++) {
            double angle = (Math.PI * 2 * i) / density;
            double radius = 0.8 + speed * 0.4;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(angle * 2) * 0.4;

            serverLevel.sendParticles(
                    corePurple,
                    position.x + x,
                    position.y + y,
                    position.z + z,
                    1,
                    -velocity.x * 0.15,
                    -velocity.y * 0.05,
                    -velocity.z * 0.15,
                    0.01
            );
        }

        // =============================
        // ⚡ CAMADA 3 — AURA ENERGÉTICA
        // =============================

        for (int i = 0; i < density / 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = 1.2 + speed * 0.3;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            serverLevel.sendParticles(
                    glowPurple,
                    position.x + x,
                    position.y + (Math.random() - 0.5),
                    position.z + z,
                    1,
                    0, 0.02, 0,
                    0.03
            );
        }

        // =============================
        // 💨 CAMADA 4 — ONDA DE CHOQUE (frente)
        // =============================

        if (speed > 1.2) {
            Vec3 front = position.add(direction.scale(1.5));

            for (int i = 0; i < 6; i++) {
                double angle = (Math.PI * 2 * i) / 6;
                double radius = 0.6;

                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        front.x + x,
                        front.y,
                        front.z + z,
                        1,
                        x * 0.2,
                        0.05,
                        z * 0.2,
                        0.2
                );
            }
        }

        // =============================
        // 💥 CAMADA 5 — RASTRO DE PRESSÃO (trás)
        // =============================

        Vec3 back = position.subtract(direction.scale(0.8));

        for (int i = 0; i < 4; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    back.x,
                    back.y + (Math.random() - 0.5),
                    back.z,
                    1,
                    -velocity.x * 0.2,
                    -velocity.y * 0.1,
                    -velocity.z * 0.2,
                    0.1
            );
        }
    }
    /**
     * Cria efeito de quebra de som direcionado (ao longo do caminho do golpe).
     */
    public static void spawnDirectionalSonicBoom(Level level, Vec3 start, Vec3 end, double intensity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        // Partículas ao longo do caminho
        int steps = Math.max(5, (int) (distance / 2));
        for (int step = 0; step < steps; step++) {
            double progress = (double) step / steps;
            Vec3 pos = start.add(direction.scale(distance * progress));

            // Partículas em espiral ao redor
            int particleCount = (int) (6 * intensity);
            for (int i = 0; i < particleCount; i++) {
                double angle = (Math.PI * 2 * i) / particleCount + (progress * 4);
                double radius = 1.0 * intensity;

                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        pos.x + x,
                        pos.y,
                        pos.z + z,
                        1,
                        x * 0.15, 0, z * 0.15,
                        0.15
                );
            }
        }
    }

    /**
     * Cria efeito de impacto com explosão de partículas.
     */
    public static void spawnImpactEffect(Level level, Vec3 position, double intensity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int explosionCount = (int) (2 + intensity * 2);
        int particleCount = (int) (20 + intensity * 10);

        // Múltiplas explosões concêntricas
        for (int e = 0; e < explosionCount; e++) {
            for (int i = 0; i < particleCount; i++) {
                // Distribuição esférica
                double theta = Math.random() * Math.PI * 2;
                double phi = Math.random() * Math.PI;
                double radius = 2.0 * intensity;

                double x = Math.sin(phi) * Math.cos(theta) * radius;
                double y = Math.cos(phi) * radius;
                double z = Math.sin(phi) * Math.sin(theta) * radius;

                serverLevel.sendParticles(
                        ParticleTypes.EXPLOSION,
                        position.x + x,
                        position.y + y,
                        position.z + z,
                        1,
                        x * 0.1, y * 0.1, z * 0.1,
                        0.5
                );
            }
        }
    }

    /**
     * Cria efeito de rastro de movimento rápido.
     */
    public static void spawnMovementTrail(Level level, Vec3 position, double velocity, int count) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Partículas "atrás" do movimento
        for (int i = 0; i < count; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    position.x,
                    position.y,
                    position.z,
                    1,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    0.1
            );
        }
    }

    /**
     * Cria efeito de crítico/super golpe.
     */
    public static void spawnCriticalHitEffect(Level level, Vec3 position) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Sparkles ao redor do ponto de impacto
        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2 * i) / 12;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;

            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    position.x + x,
                    position.y + 1,
                    position.z + z,
                    1,
                    x * 0.2, 0.2, z * 0.2,
                    0.3
            );
        }
    }
}
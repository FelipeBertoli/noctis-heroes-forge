package com.noctisheroes.entity.abilities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Gerenciador de partículas para efeitos visuais de abilities.
 *
 * ✅ Recursos:
 * - Partículas de quebra de som (sonic boom)
 * - Partículas de impacto
 * - Partículas de rastro de movimento
 * - Efeitos configuráveis
 */
public class AbilityParticleEffects {

    /**
     * Cria efeito de quebra de som ao redor da entidade.
     * Simula compressão do ar (como um sonic boom).
     */
    public static void spawnSonicBoomEffect(Level level, Vec3 position, double intensity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int particleCount = (int) (8 + intensity * 4);
        double radius = 1.5 * intensity;

        // Partículas ao redor (esfera)
        for (int i = 0; i < particleCount; i++) {
            double angle = (Math.PI * 2 * i) / particleCount;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (Math.random() - 0.5) * radius;

            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    position.x + x,
                    position.y + y,
                    position.z + z,
                    1,
                    x * 0.1, y * 0.1, z * 0.1,
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
            double delay = e * 0.1;
            int delayTicks = (int) (delay * 20);

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
            double offset = velocity * (i * 0.5);

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
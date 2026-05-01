package com.noctisheroes.common.ability.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Detector de impacto para abilities - verifica colisão com blocos e entidades.
 *
 * ✅ Recursos:
 * - Detecção de colisão com blocos sólidos
 * - Cálculo de ponto de impacto preciso
 * - Raycasting para verificação de caminho
 * - Performance otimizada
 */
public class ImpactDetector {

    private static final double IMPACT_CHECK_RADIUS = 2.0;
    private static final double RAYCAST_STEP = 0.1;

    /**
     * Verifica se há um impacto próximo à posição.
     * Retorna true se houver um bloco sólido próximo.
     */
    public static boolean hasNearbyCollision(Level level, Vec3 position, double radius) {
        BlockPos center = BlockPos.containing(position);

        // Verifica blocos sólidos em um raio
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    if (isBlockSolid(level, checkPos)) {
                        double dist = position.distanceTo(Vec3.atCenterOf(checkPos));
                        if (dist <= radius) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Encontra o bloco de impacto mais próximo.
     * Retorna null se nenhum bloco encontrado.
     */
    public static BlockPos findNearestImpactBlock(Level level, Vec3 position, double radius) {
        BlockPos center = BlockPos.containing(position);
        BlockPos nearest = null;
        double minDistance = radius;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    if (isBlockSolid(level, checkPos)) {
                        double dist = position.distanceTo(Vec3.atCenterOf(checkPos));
                        if (dist < minDistance) {
                            minDistance = dist;
                            nearest = checkPos;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    /**
     * Detecta colisão através de raycasting ao longo do caminho.
     */
    public static ImpactResult detectImpactAlongPath(Level level, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        // Raycasting em passos pequenos
        for (double i = 0; i <= distance; i += RAYCAST_STEP) {
            Vec3 checkPos = start.add(direction.scale(i));
            BlockPos blockPos = BlockPos.containing(checkPos);

            if (isBlockSolid(level, blockPos)) {
                return new ImpactResult(true, checkPos, blockPos);
            }
        }

        return new ImpactResult(false, null, null);
    }

    /**
     * Verifica se um bloco é sólido (pode causar impacto).
     */
    private static boolean isBlockSolid(Level level, BlockPos pos) {
        var blockState = level.getBlockState(pos);

        // ignora ar
        if (blockState.isAir()) {
            return false;
        }

        // checa se é substituível
        if (blockState.canBeReplaced()) {
            return false;
        }

        // checa colisão real (mais confiável que material)
        return !blockState.getCollisionShape(level, pos).isEmpty();
    }

    /**
     * Resultado de detecção de impacto.
     */
    public static class ImpactResult {
        public final boolean impacted;
        public final Vec3 impactPosition;
        public final BlockPos impactBlockPos;

        public ImpactResult(boolean impacted, Vec3 impactPosition, BlockPos impactBlockPos) {
            this.impacted = impacted;
            this.impactPosition = impactPosition;
            this.impactBlockPos = impactBlockPos;
        }
    }
}
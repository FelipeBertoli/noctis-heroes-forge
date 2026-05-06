package com.noctisheroes.common.ability.helpers;

import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


public class AbilityHelper {

    public static void dashToTarget(NoctisEntity entity, LivingEntity target, double speed) {
        Vec3 direction = target.position()
                .subtract(entity.position())
                .normalize();

        entity.setDeltaMovement(direction.scale(speed));
    }

    public static boolean isNearImpact(Level level, Vec3 pos, double radius) {
        return ImpactDetector.hasNearbyCollision(level, pos, radius);
    }

    public static float scaledAttack(NoctisEntity entity, float multiplier) {
        float base = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return base * multiplier;
    }

    public static void applyKnockback(LivingEntity target, Vec3 direction, double strength, double vertical) {
        target.setDeltaMovement(
                direction.x * strength,
                vertical,
                direction.z * strength
        );
    }

    public static void destroyPath(Level level, Vec3 start, Vec3 end, float radius) {
        if (!(level instanceof ServerLevel)) return;

        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1) return;

        direction = direction.normalize();

        int steps = (int) (distance * 3);

        for (int i = 0; i < steps; i++) {
            double progress = (double) i / steps;
            Vec3 pos = start.add(direction.scale(distance * progress));

            destroySphere(level, pos, radius);
        }
    }

    public static void destroySphere(Level level, Vec3 center, float radius) {
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
                    if (state.getDestroySpeed(level, pos) > 10f) continue;

                    level.destroyBlock(pos, true);
                }
            }
        }
    }

    public static boolean hasSurface(Level level, LivingEntity entity, double radius) {
        return ImpactDetector.hasNearbyCollision(level, entity.position(), radius);
    }

    public static void explode(Level level, NoctisEntity source, Vec3 pos, float radius) {
        level.explode(
                source,
                pos.x,
                pos.y,
                pos.z,
                radius,
                Level.ExplosionInteraction.MOB
        );
    }
}

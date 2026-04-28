package com.noctisheroes.entity.abilities;

import com.noctisheroes.entity.base.NoctisEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.RawAnimation;

public class DestructiveDashAbility implements NoctisAbility<NoctisEntity> {

    private int ticks;

    private boolean hitApplied;
    private boolean launchedTarget;

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
        return dist > 5 && dist < 18 && entity.getRandom().nextFloat() < 0.03f;
    }

    @Override
    public void start(NoctisEntity entity) {
        ticks = 0;
        hitApplied = false;
        launchedTarget = false;

        // imunidade temporária (você precisa ter esse flag no entity depois)
        entity.setNoGravity(true);
    }

    @Override
    public void tick(NoctisEntity entity) {
        ticks++;

        var target = entity.getTarget();
        if (target == null) return;

        Level level = entity.level();

        // =============================
        // 🚀 DASH INICIAL
        // =============================
        if (ticks <= 5) {
            Vec3 dir = target.position()
                    .subtract(entity.position())
                    .normalize();

            entity.setDeltaMovement(dir.scale(2.8));
        }

        // =============================
        // 💥 IMPACTO DO SOCÃO
        // =============================
        if (ticks == 6 && !hitApplied) {

            hitApplied = true;

            double dist = entity.distanceTo(target);

            if (dist < 3.5) {

                // dano massivo
                target.hurt(entity.damageSources().mobAttack(entity), 35.0F);

                // knockback absurdo
                Vec3 knockDir = target.position()
                        .subtract(entity.position())
                        .normalize()
                        .scale(4.5);

                target.setDeltaMovement(knockDir.x, 0.6, knockDir.z);

                launchedTarget = true;

                // explosão no ponto do soco
                level.explode(
                        entity,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        3.0F,
                        Level.ExplosionInteraction.MOB
                );
            }
        }

        // =============================
        // 🌍 IMPACTO COM BLOCO (REAÇÃO SECUNDÁRIA)
        // =============================
        if (launchedTarget) {
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    player.displayClientMessage(
                            Component.literal("Launched Target"),
                            false
                    );
                }
            }
            if (target.horizontalCollision || target.verticalCollision) {
                if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    for (ServerPlayer player : serverLevel.players()) {
                        player.displayClientMessage(
                                Component.literal("Atingiu superficie"),
                                false
                        );
                    }
                }
                level.explode(
                        entity,
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        2.0F,
                        Level.ExplosionInteraction.MOB
                );

                launchedTarget = false;
            }
        }

        // =============================
        // 🧊 FINALIZAÇÃO
        // =============================
        if (ticks > 12) {
            stop(entity);
        }
    }

    @Override
    public void stop(NoctisEntity entity) {
        entity.setNoGravity(false);
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
    }

    @Override
    public boolean isFinished(NoctisEntity entity) {
        return ticks > 14;
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
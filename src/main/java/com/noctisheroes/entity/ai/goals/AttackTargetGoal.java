package com.noctisheroes.entity.ai.goals;

import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;


public class AttackTargetGoal extends Goal {

    protected final NoctisEntity mob;

    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;

    private Path path;

    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;

    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;

    private long lastCanUseCheck;

    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

    public AttackTargetGoal(
            NoctisEntity mob,
            double speedModifier,
            boolean followingTargetEvenIfNotSeen
    ) {

        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    // =========================================
    // ✅ CAN USE
    // =========================================

    @Override
    public boolean canUse() {

        // 🔥 não pode atacar bloqueando
        if (mob.isBlocking()) {
            return false;
        }

        // 🔥 ability impede ataque
        if (
                mob.getAbilityManager().isUsingAbility()
                        && mob.getAbilityManager()
                        .getCurrentAbility()
                        .preventsAttacking()
        ) {
            return false;
        }

        long gameTime =
                mob.level().getGameTime();

        if (
                gameTime - lastCanUseCheck
                        < COOLDOWN_BETWEEN_CAN_USE_CHECKS
        ) {
            return false;
        }

        lastCanUseCheck = gameTime;

        LivingEntity target =
                mob.getTarget();

        if (target == null) {
            return false;
        }

        if (!target.isAlive()) {
            return false;
        }

        path =
                mob.getNavigation()
                        .createPath(target, 0);

        if (path != null) {
            return true;
        }

        return getAttackReachSqr(target)
                >= mob.distanceToSqr(
                target.getX(),
                target.getY(),
                target.getZ()
        );
    }

    // =========================================
    // 🔄 CONTINUE
    // =========================================

    @Override
    public boolean canContinueToUse() {

        if (mob.isBlocking()) {
            return false;
        }

        if (
                mob.getAbilityManager().isUsingAbility()
                        && mob.getAbilityManager()
                        .getCurrentAbility()
                        .preventsAttacking()
        ) {
            return false;
        }

        LivingEntity target =
                mob.getTarget();

        if (target == null) {
            return false;
        }

        if (!target.isAlive()) {
            return false;
        }

        if (!followingTargetEvenIfNotSeen) {
            return !mob.getNavigation().isDone();
        }

        if (!mob.isWithinRestriction(target.blockPosition())) {
            return false;
        }

        return !(
                target instanceof Player player
                        && (
                        player.isCreative()
                                || player.isSpectator()
                )
        );
    }

    // =========================================
    // 🚀 START
    // =========================================

    @Override
    public void start() {

        mob.getNavigation()
                .moveTo(path, speedModifier);

        mob.setAggressive(true);

        ticksUntilNextPathRecalculation = 0;
        ticksUntilNextAttack = 0;
    }

    // =========================================
    // 🛑 STOP
    // =========================================

    @Override
    public void stop() {

        LivingEntity target =
                mob.getTarget();

        if (
                !EntitySelector.NO_CREATIVE_OR_SPECTATOR
                        .test(target)
        ) {
            mob.setTarget(null);
        }

        mob.setAggressive(false);

        mob.getNavigation().stop();
    }

    // =========================================
    // 🔄 UPDATE
    // =========================================

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // =========================================
    // 🔄 TICK
    // =========================================

    @Override
    public void tick() {

        // 🔥 trava tudo durante block
        if (mob.isBlocking()) {

            mob.getNavigation().stop();

            return;
        }

        LivingEntity target =
                mob.getTarget();

        if (target == null) {
            return;
        }

        mob.getLookControl()
                .setLookAt(
                        target,
                        30.0F,
                        30.0F
                );

        double distance =
                mob.getPerceivedTargetDistanceSquareForMeleeAttack(target);

        ticksUntilNextPathRecalculation =
                Math.max(
                        ticksUntilNextPathRecalculation - 1,
                        0
                );

        if (
                (
                        followingTargetEvenIfNotSeen
                                || mob.getSensing().hasLineOfSight(target)
                )
                        && ticksUntilNextPathRecalculation <= 0
        ) {

            pathedTargetX = target.getX();
            pathedTargetY = target.getY();
            pathedTargetZ = target.getZ();

            ticksUntilNextPathRecalculation =
                    4 + mob.getRandom().nextInt(7);

            if (distance > 1024.0D) {
                ticksUntilNextPathRecalculation += 10;
            }

            else if (distance > 256.0D) {
                ticksUntilNextPathRecalculation += 5;
            }

            if (
                    !mob.getNavigation()
                            .moveTo(target, speedModifier)
            ) {
                ticksUntilNextPathRecalculation += 15;
            }

            ticksUntilNextPathRecalculation =
                    adjustedTickDelay(
                            ticksUntilNextPathRecalculation
                    );
        }

        ticksUntilNextAttack =
                Math.max(
                        ticksUntilNextAttack - 1,
                        0
                );

        checkAndPerformAttack(target, distance);
    }

    // =========================================
    // ⚔️ ATTACK
    // =========================================

    protected void checkAndPerformAttack(
            LivingEntity target,
            double distance
    ) {

        // 🔥 segurança extra
        if (mob.isBlocking()) {
            return;
        }

        double reach =
                getAttackReachSqr(target);

        if (
                distance <= reach
                        && ticksUntilNextAttack <= 0
        ) {

            resetAttackCooldown();

            mob.swing(InteractionHand.MAIN_HAND);

            mob.doHurtTarget(target);
        }
    }

    // =========================================
    // ⏳ COOLDOWN
    // =========================================

    protected void resetAttackCooldown() {
        ticksUntilNextAttack =
                adjustedTickDelay(20);
    }

    protected double getAttackReachSqr(
            LivingEntity target
    ) {

        return (
                mob.getBbWidth()
                        * 2.0F
                        * mob.getBbWidth()
                        * 2.0F
        ) + target.getBbWidth();
    }
}
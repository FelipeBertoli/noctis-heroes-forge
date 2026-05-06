package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.AbilityHelper;
import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.RawAnimation;

public class DestructiveDashAbility extends TimedAbility<NoctisEntity> {

    private LivingEntity hitTarget;
    private boolean impactTriggered;

    @Override
    protected void onStart(NoctisEntity entity) {
        entity.setNoGravity(true);
        hitTarget = null;
        impactTriggered = false;
    }

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {

        LivingEntity target = entity.getTarget();
        if (target == null) return;

        if (ticks <= 5) {
            AbilityHelper.dashToTarget(entity, target, 2.8);
        }

        if (ticks == 6) {
            float damage = AbilityHelper.scaledAttack(entity, 1.5f);
            target.hurt(entity.damageSources().mobAttack(entity), damage);
            hitTarget = target;
        }

        if (ticks > 6 && !impactTriggered) {
            if (AbilityHelper.isNearImpact(entity.level(), target.position(), 3)) {
                entity.level().explode(entity,
                        target.getX(), target.getY(), target.getZ(),
                        2.0f,
                        Level.ExplosionInteraction.MOB);

                impactTriggered = true;
            }
        }
    }

    @Override
    protected void onStop(NoctisEntity entity) {
        entity.setNoGravity(false);
    }

    @Override
    protected int getDuration() {
        return 20;
    }

    @Override
    public String getId() { return "destructive_dash"; }

    @Override
    public int getCooldown() { return 100; }

    @Override
    public int getPriority() { return 10; }

    @Override
    public boolean canUse(NoctisEntity entity) {
        var target = entity.getTarget();
        if (target == null) return false;

        double dist = entity.distanceTo(target);
        return dist > 5 && dist < 18;
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
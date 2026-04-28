package com.noctisheroes.entity.base;

import com.noctisheroes.entity.base.goals.AerialOrbitGoal;
import com.noctisheroes.entity.base.goals.FlyingChaseGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

public abstract class AbstractViltrumite extends NoctisEntity {

    // =============================
    // 🎬 ANIMAÇÕES
    // =============================

    protected static final RawAnimation LEVITATE_IDLE =
            RawAnimation.begin().thenLoop("animation.viltrumite.levitate_idle");
    protected static final RawAnimation LEVITATE_FLIGHT=
            RawAnimation.begin().thenLoop("animation.viltrumite.levitate_flight");
    // =============================
    // 🔄 SYNC DATA (CRÍTICO)
    // =============================

    private static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(AbstractViltrumite.class, EntityDataSerializers.BOOLEAN);

    public AbstractViltrumite(EntityType<? extends Monster> type, Level level, String tag) {
        super(type, level, tag);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLYING, false);
    }

    // =============================
    // 🧠 ESTADO DE VOO
    // =============================

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
        this.setNoGravity(flying);
    }

    // =============================
    // 🎯 IA
    // =============================

    @Override
    protected void registerGoals() {

        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, true));
        goalSelector.addGoal(3, new FlyingChaseGoal(this));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(AbstractViltrumite.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
    }

    // =============================
    // 🎬 CONTROLLER DE ANIMAÇÃO
    // =============================

    @Override
    protected <E extends NoctisEntity> PlayState walkController(final AnimationState<E> event) {

        var controller = event.getController();

        if (isFlying()) {

            if (event.isMoving() && this.getDeltaMovement().lengthSqr() > 0.002) {
                controller.setAnimation(LEVITATE_FLIGHT);
            } else {
                controller.setAnimation(LEVITATE_IDLE);
            }

            return PlayState.CONTINUE;
        }
        if (event.isMoving() && this.getDeltaMovement().lengthSqr() > 0.002) {
            controller.setAnimation(WALK_ANIM);
        } else {
            controller.setAnimation(IDLE_ANIM);
        }

        return PlayState.CONTINUE;
    }

    // =============================
    // 🧭 PATHFINDING (VOO)
    // =============================

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    // =============================
    // 🚀 MOVIMENTO
    // =============================

    @Override
    public void travel(Vec3 travelVector) {

        if (isFlying()) {
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
        } else {
            super.travel(travelVector);
        }
    }

    // =============================
    // 🔄 LÓGICA DE TRANSIÇÃO
    // =============================

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        // alternância aleatória
        if (this.random.nextFloat() < 0.002f) {
            setFlying(!isFlying());
        }

        // combate → tende a voar
        if (this.getTarget() != null) {
            if (this.random.nextFloat() < 0.01f) {
                setFlying(true);
            }
        }

        // impulso ao levantar voo
        if (isFlying() && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, 0.5, 0));
        }
    }
    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }
}
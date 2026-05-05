package com.noctisheroes.entity.entities.base;

import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.goals.FlyingChaseGoal;
import com.noctisheroes.entity.ai.states.FlightState;
import com.noctisheroes.entity.components.FlightWarriorComponent;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class AbstractFlightWarrior extends NoctisEntity implements IFlightCapable {

    protected static final RawAnimation LEVITATE_IDLE = RawAnimation.begin().thenLoop("animation.humanoid.levitate_idle");
    protected static final RawAnimation LEVITATE_FLIGHT = RawAnimation.begin().thenLoop("animation.humanoid.levitate_flight");
    protected static final RawAnimation FLIGHT_START = RawAnimation.begin().thenPlay("animation.humanoid.flight_start");
    protected static final RawAnimation HUNT_FLIGHT = RawAnimation.begin().thenLoop("animation.humanoid.hunt_flight");
    protected static final RawAnimation FLIGHT_STOP = RawAnimation.begin().thenPlay("animation.humanoid.flight_stop");
    private static final EntityDataAccessor<Integer> FLIGHT_STATE = SynchedEntityData.defineId(AbstractFlightWarrior.class, EntityDataSerializers.INT);
    private final FlightWarriorComponent flightComponent = new FlightWarriorComponent(this);
    private FlyingPathNavigation cachedFlyingNavigation;
    protected static final float RANDOM_FLIGHT_TOGGLE_CHANCE = 0.002f;
    protected static final float COMBAT_FLIGHT_CHANCE = 0.01f;

    protected AbstractFlightWarrior(EntityType<? extends Monster> type, Level level, String tag, AttributeConfig attributes, EntityConfig config) {
        super(type, level, tag, attributes, config);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    // =============================
    // 📊 SYNC DATA
    // =============================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLIGHT_STATE, FlightState.GROUNDED.ordinal());
    }

    @Override
    public FlightState getFlightState() {
        return FlightState.values()[this.entityData.get(FLIGHT_STATE)];
    }

    @Override
    public void setFlightState(FlightState state) {
        if (this.getFlightState() == state) return;

        int ticksSinceChange = flightComponent.getTicksSinceFlightChange();
        if (ticksSinceChange < 10) return;

        this.entityData.set(FLIGHT_STATE, state.ordinal());
        this.setNoGravity(state != FlightState.GROUNDED);
        flightComponent.resetFlightChangeTimer();
    }

    @Override
    public void forceSetFlightState(FlightState state) {
        this.entityData.set(FLIGHT_STATE, state.ordinal());
        this.setNoGravity(state != FlightState.GROUNDED);
        flightComponent.resetFlightChangeTimer();
    }

    @Override
    public FlyingPathNavigation getFlyingNavigation() {
        if (this.cachedFlyingNavigation == null) {
            this.cachedFlyingNavigation = new FlyingPathNavigation(this, this.level());
            this.cachedFlyingNavigation.setCanOpenDoors(false);
            this.cachedFlyingNavigation.setCanFloat(true);
        }
        return this.cachedFlyingNavigation;
    }

    @Override
    public float getCombatFlightChance() {
        return COMBAT_FLIGHT_CHANCE;
    }

    @Override
    public float getRandomFlightToggleChance() {
        return RANDOM_FLIGHT_TOGGLE_CHANCE;
    }

    // =============================
    // 🎯 GOALS
    // =============================

    @Override
    protected void registerMovementGoals() {
        goalSelector.addGoal(3, new FlyingChaseGoal(this, this));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    protected void registerLookingGoals() {
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // =============================
    // 🎬 ANIMAÇÃO
    // =============================

    @Override
    protected <E extends NoctisEntity> PlayState movementController(AnimationState<E> event) {
        var controller = event.getController();
        FlightState state = getFlightState();

        switch (state) {
            case LEVITATE -> {
                if (event.isMoving()) {
                    controller.setAnimation(LEVITATE_FLIGHT);
                } else {
                    controller.setAnimation(LEVITATE_IDLE);
                }
            }
            case FLIGHT_START -> controller.setAnimation(FLIGHT_START);
            case HUNT_FLIGHT -> controller.setAnimation(HUNT_FLIGHT);
            case FLIGHT_STOP -> controller.setAnimation(FLIGHT_STOP);
            case GROUNDED -> {
                return super.movementController(event);
            }
        }

        if (this.getAbilityManager().isUsingAbility()) {
            return PlayState.STOP;
        }

        return PlayState.CONTINUE;
    }

    // =============================
    // 🧭 PATHFINDING
    // =============================

    @Override
    protected PathNavigation createNavigation(Level level) {
        this.cachedFlyingNavigation = new FlyingPathNavigation(this, level);
        this.cachedFlyingNavigation.setCanOpenDoors(false);
        this.cachedFlyingNavigation.setCanFloat(true);
        return this.cachedFlyingNavigation;
    }

    // =============================
    // 🚀 MOVIMENTO
    // =============================

    @Override
    public void travel(Vec3 travelVector) {
        if (getFlightState() != FlightState.GROUNDED) {
            // ✅ Usar componente para movimento de voo
            flightComponent.applyFlightMovement(travelVector, this);
        } else {
            super.travel(travelVector);
        }
    }

    // =============================
    // 🔄 TICK
    // =============================

    @Override
    public void tick() {
        super.tick();

        // ✅ Delegar lógica de voo ao componente
        flightComponent.tick(this);
    }
}

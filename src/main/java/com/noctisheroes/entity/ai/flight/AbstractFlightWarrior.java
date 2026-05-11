package com.noctisheroes.entity.ai.flight;

import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.goals.DynamicTargetGoal;
import com.noctisheroes.entity.ai.goals.FlyingChaseGoal;
import com.noctisheroes.entity.animation.FlightHumanoidAnimations;
import com.noctisheroes.entity.animation.IEntityAnimations;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFlightWarrior
        extends NoctisEntity
        implements IFlightCapable {


    private static final EntityDataAccessor<Integer> FLIGHT_STATE = SynchedEntityData.defineId(AbstractFlightWarrior.class, EntityDataSerializers.INT);
    protected static final int BLOCK_DESTROY_RADIUS = 2;
    protected static final int BLOCK_DESTROY_COOLDOWN = 5;
    private int blockDestroyTicker = 0;
    private static final IEntityAnimations DEFAULT_ANIMATIONS = new FlightHumanoidAnimations();
    private final FlightWarriorComponent flightComponent;
    private FlyingPathNavigation flyingNavigation;

    // =========================================
    // 🏗️ CONSTRUCTORS
    // =========================================

    protected AbstractFlightWarrior(EntityType<? extends Monster> type, Level level, String tag, AttributeConfig attributes, EntityConfig config) {
        this(type, level, tag, attributes, config, DEFAULT_ANIMATIONS);
    }

    protected AbstractFlightWarrior(EntityType<? extends Monster> type, Level level, String tag, AttributeConfig attributes, EntityConfig config, IEntityAnimations animations) {
        super( type, level, tag, attributes, config, animations);
        this.flightComponent = new FlightWarriorComponent(this);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    // =========================================
    // 📡 ENTITY DATA
    // =========================================

    @Override
    protected void defineSynchedData() {

        super.defineSynchedData();

        entityData.define(
                FLIGHT_STATE,
                FlightState.GROUNDED.ordinal()
        );
    }

    // =========================================
    // 🪁 FLIGHT INTERFACE - IFlightCapable
    // =========================================

    @Override
    public FlightState getFlightState() {
        return FlightState.values()[ entityData.get(FLIGHT_STATE)];
    }

    @Override
    public void setFlightState(FlightState state) {

        if (getFlightState() == state) {return;}

        entityData.set(FLIGHT_STATE, state.ordinal());

        // ✅ Aplica/remove gravidade baseado no estado
        setNoGravity(state != FlightState.GROUNDED);
    }

    @Override
    public void forceSetFlightState(FlightState state) {

        entityData.set(FLIGHT_STATE, state.ordinal());

        setNoGravity(state != FlightState.GROUNDED);
    }

    // =========================================
    // ✈️ NAVIGATION
    // =========================================

    @Override
    protected PathNavigation createNavigation(Level level) {
        flyingNavigation = new FlyingPathNavigation(this, level);
        flyingNavigation.setCanFloat(true);
        return flyingNavigation;
    }

    // =========================================
    // 🧠 AI GOALS
    // =========================================

    @Override
    protected void registerMovementGoals() {
        goalSelector.addGoal(2, new FlyingChaseGoal(this, this, flightComponent));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    protected void registerLookingGoals() {
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    protected void registerTargetGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new DynamicTargetGoal<>(this, Player.class, 2.0, true, 20));
        targetSelector.addGoal(3, new DynamicTargetGoal<>(this, AbstractVillager.class, true));
        targetSelector.addGoal(4, new DynamicTargetGoal<>(this, IronGolem.class, true));
    }

    // =========================================
    // 🔄 TICK
    // =========================================

    @Override
    public void tick() {

        // ✅ Atualiza componente de voo
        if (!level().isClientSide) {
            flightComponent.tick(this);
            if (getFlightState() != FlightState.GROUNDED && onGround()) {setFlightState(FlightState.GROUNDED);}
            tickBlockDestruction();
        }

        super.tick();
    }

    // =========================================
    // 🚗 MOVEMENT
    // =========================================

    @Override
    public void travel(Vec3 vec) {

        if (getFlightState() != FlightState.GROUNDED) {
            move(MoverType.SELF, getDeltaMovement());
            return;
        }

        super.travel(vec);
    }

    // =========================================
    // 🌍 INITIAL FLIGHT STATE
    // =========================================

    protected void determineInitialFlightState() {

        BlockPos pos = blockPosition();

        for (int i = 1; i <= 3; i++) {

            pos = pos.below();

            BlockState state = level().getBlockState(pos);

            if (!state.getCollisionShape(level(), pos).isEmpty()) {
                forceSetFlightState(FlightState.GROUNDED);
                return;
            }
        }

        forceSetFlightState(FlightState.LEVITATE);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag dataTag) {
        var result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
        determineInitialFlightState();
        return result;
    }

    // =========================================
    // 💥 BLOCK DESTRUCTION
    // =========================================

    private void tickBlockDestruction() {

        // Só destrói blocos em HUNT_FLIGHT
        if (getFlightState() != FlightState.HUNT_FLIGHT) {
            blockDestroyTicker = 0;
            return;
        }

        blockDestroyTicker++;

        // Só tenta destruir a cada X ticks (cooldown)
        if (blockDestroyTicker < BLOCK_DESTROY_COOLDOWN) {
            return;
        }

        blockDestroyTicker = 0;

        BlockPos entityPos = this.blockPosition();
        int radius = getBlockDestroyRadius();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = entityPos.offset(x, y, z);
                    destroyBlockIfValid(targetPos);
                }
            }
        }
    }

    private void destroyBlockIfValid(BlockPos pos) {

        BlockState block = this.level().getBlockState(pos);

        // ✅ Não destrói blocos "indestrutíveis"
        if (isBlockIndestructible(block)) {
            return;
        }

        // ✅ Só destrói se for um bloco sólido
        if (!block.isAir() && block.isSolidRender(this.level(), pos)) {
            this.level().destroyBlock(pos, true); // true = droppa items
        }
    }

    protected boolean isBlockIndestructible(BlockState block) {return block.getDestroySpeed(this.level(), BlockPos.ZERO) < 0;}
    protected int getBlockDestroyRadius() {return BLOCK_DESTROY_RADIUS;}
    protected int getBlockDestroyCooldown() { return BLOCK_DESTROY_COOLDOWN;}


    public FlightWarriorComponent getFlightComponent() {return flightComponent;}
    public FlightState getCurrentFlightState() {return getFlightState();}
    public FlyingPathNavigation getFlyingNavigation() {return flyingNavigation;}
}
package com.noctisheroes.entity;

import com.noctisheroes.common.ability.abilities.BlockAbility;
import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.combat.rage.RageConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.ability.helpers.AbilityManager;
import com.noctisheroes.common.combat.damage.DamageConfig;
import com.noctisheroes.common.combat.damage.DamageManager;
import com.noctisheroes.common.effect.EffectManager;
import com.noctisheroes.entity.animation.AnimationResolver;
import com.noctisheroes.entity.ai.states.VisualState;
import com.noctisheroes.entity.animation.AnimationKey;
import com.noctisheroes.entity.animation.FlightHumanoidAnimations;
import com.noctisheroes.entity.animation.IEntityAnimations;
import com.noctisheroes.entity.components.RageComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class NoctisEntity extends Monster implements GeoEntity {

    // =========================================
    // 🎞️ ANIMATION
    // =========================================
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final IEntityAnimations animations;
    private AnimationKey currentActionAnimation;
    private int actionAnimationTicks = 0;
    private boolean lastAttackRight = false;
    private AnimationKey lastMovementAnimation;
    private boolean isBlocking = false;

    // =========================================
    // 📡 ENTITY DATA
    // =========================================
    private static final EntityDataAccessor<Integer> SKIN_ID = SynchedEntityData.defineId(NoctisEntity.class, EntityDataSerializers.INT);

    // =========================================
    // ⚔️ SYSTEMS
    // =========================================
    private final DamageManager damageManager;
    private final AbilityManager<NoctisEntity> abilityManager = new AbilityManager<>();
    private final EffectManager effectManager = new EffectManager();
    private RageComponent rage;

    // =========================================
    // 📦 CONFIG
    // =========================================
    private final String entityTag;
    private final EntityConfig config;
    private final AttributeConfig attributeConfig;

    // =========================================
    // 🏗️ CONSTRUCTOR
    // =========================================

    protected NoctisEntity(
            EntityType<? extends Monster> type,
            Level level,
            String tag,
            AttributeConfig attributes,
            EntityConfig config,
            IEntityAnimations animations
    ) {
        super(type, level);
        this.entityTag = tag;
        this.attributeConfig = attributes;
        this.config = config;
        this.animations = animations;
        this.damageManager = new DamageManager();
        this.xpReward = config.xpReward;
    }

    // =========================================
    // 😡 RAGE
    // =========================================

    protected void initRage(RageConfig config) {
        this.rage = new RageComponent(this);
    }

    public RageComponent getRage() {
        return rage;
    }

    // =========================================
    // 📡 ENTITY DATA
    // =========================================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_ID, 0);
    }

    public final int getSkinId() {
        return this.entityData.get(SKIN_ID);
    }

    protected final void setSkinId(int id) {
        if (id >= 0 && id < getSkinCount()) this.entityData.set(SKIN_ID, id);
    }

    // =========================================
    // 🎞️ GECKOLIB
    // =========================================

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "locomotion", 2, this::locomotionController));
        registrar.add(
                new AnimationController<>(this, "actions", state -> PlayState.STOP)
                        .triggerableAnim("right_attack", animations.getAnimation(AnimationKey.RIGHT_ATTACK))
                        .triggerableAnim("left_attack", animations.getAnimation(AnimationKey.LEFT_ATTACK))
                        .triggerableAnim("block", animations.getAnimation(AnimationKey.BLOCK))
        );
    }



    protected <E extends NoctisEntity>
    PlayState locomotionController(AnimationState<E> event) {

        AnimationKey key = AnimationResolver.resolveMovement(this);

        if (key == null) return PlayState.STOP;
        RawAnimation animation = animations.getAnimation(key);

        if (animation == null) return PlayState.STOP;

        if (lastMovementAnimation != key) {
            event.getController().forceAnimationReset();
            lastMovementAnimation = key;
            if (animations instanceof FlightHumanoidAnimations flight) if (key == AnimationKey.HUNT_FLIGHT) flight.resetHuntFlightVariation();
        }

        event.setAndContinue(animation);

        return PlayState.CONTINUE;
    }

    // =========================================
    // ⚔️ ACTION CONTROLLER
    // =========================================

    private AnimationKey queuedActionAnimation;

    public void playActionAnimation(AnimationKey key) {
        this.queuedActionAnimation = key;
    }

    protected <E extends NoctisEntity>
    PlayState actionController(AnimationState<E> event) {

        var controller = event.getController();

        if (queuedActionAnimation != null) {
            RawAnimation animation = animations.getAnimation(queuedActionAnimation);
            queuedActionAnimation = null;

            if (animation != null) {
                controller.forceAnimationReset();
                controller.setAnimation(animation);
                return PlayState.CONTINUE;
            }
        }

        if (controller.getAnimationState() != AnimationController.State.STOPPED) return PlayState.CONTINUE;

        return PlayState.STOP;
    }
    // =========================================
    // 🥊 BASIC ATTACKS
    // =========================================

    @Override
    public boolean doHurtTarget(Entity target) {
        triggerAnim("actions", lastAttackRight ? "right_attack" : "left_attack");
        lastAttackRight = !lastAttackRight;
        return super.doHurtTarget(target);
    }

    // =========================================
    // 🧠 AI GOALS
    // =========================================

    @Override
    protected void registerGoals() {
        registerMovementGoals();
        registerCombatGoals();
        registerLookingGoals();
        registerTargetGoals();
    }

    protected void registerMovementGoals() {
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    protected void registerCombatGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
    }

    protected void registerLookingGoals() {
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    protected void registerTargetGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
    }

    // =========================================
    // 🔄 TICK
    // =========================================

    @Override
    public void tick() {
        super.tick();

        if (actionAnimationTicks > 0) {
            actionAnimationTicks--;
            if (actionAnimationTicks <= 0) currentActionAnimation = null;
        }

        if (!level().isClientSide) {
            abilityManager.tick(this);
            effectManager.tick(this);
            if (rage != null) rage.tick();
        }
    }

    // =========================================
    // 💥 DAMAGE
    // =========================================

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(this.isBlocking) return false;
        DamageConfig ctx = new DamageConfig(source);
        if (!damageManager.canBeDamaged(ctx)) return false;
        amount = damageManager.applyModifiers( ctx, amount );
        if (rage != null) rage.addFromDamage(amount);

        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        if (!damageManager.shouldTakeFallDamage()) return false;

        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    // =========================================
    // 📦 SPAWN
    // =========================================

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            SpawnGroupData spawnData,
            CompoundTag tag
    ) {
        setSkinId(random.nextInt(getSkinCount()));

        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }

    // =========================================
    // 💾 SAVE DATA
    // =========================================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", getSkinId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId")) setSkinId(tag.getInt("SkinId"));
    }

    // =========================================
    // 📊 VISUAL STATE
    // =========================================

    public VisualState getVisualState() {
        float hp = getHealth();
        float max = getMaxHealth();
        float threshold = config.damageThreshold;
        if (hp / max <= threshold) return VisualState.DAMAGED;

        return VisualState.NORMAL;
    }

    // =========================================
    // 📦 GETTERS
    // =========================================

    public String getEntityTag() {
        return entityTag;
    }

    public DamageManager getDamageProfile() {
        return damageManager;
    }

    public AbilityManager<NoctisEntity> getAbilityManager() {
        return abilityManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public AttributeConfig getAttributeConfig() {
        return attributeConfig;
    }

    public IEntityAnimations getAnimations() {
        return animations;
    }

    protected int getSkinCount() {
        return config.skinCount;
    }

    @Override
    public boolean isBlocking() {
        return isBlocking;
    }

    public void setBlocking(boolean blocking) {
        isBlocking = blocking;
    }


}
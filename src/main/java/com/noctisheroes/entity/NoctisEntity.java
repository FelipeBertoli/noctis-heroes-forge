package com.noctisheroes.entity;

import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.ability.helpers.AbilityManager;
import com.noctisheroes.common.combat.damage.DamageConfig;
import com.noctisheroes.common.attribute.AttributeManager;
import com.noctisheroes.common.combat.damage.DamageManager;
import com.noctisheroes.common.effect.EffectManager;
import com.noctisheroes.entity.ai.states.VisualState;
import com.noctisheroes.entity.handlers.MeleeAttackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
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
    // =============================
    // 📦 CONSTANTES & CACHE
    // =============================


    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.humanoid.walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.humanoid.idle");

    public static final RawAnimation RIGHT_ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.humanoid.right_attack");
    public static final RawAnimation LEFT_ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.humanoid.left_attack");

    private boolean wasDamaged = false;
    private static final EntityDataAccessor<Integer> SKIN_ID =
            SynchedEntityData.defineId(NoctisEntity.class, EntityDataSerializers.INT);

    // =============================
    // 🎯 CAMPOS ENCAPSULADOS
    // =============================

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final MeleeAttackHandler<NoctisEntity> attackHandler = new MeleeAttackHandler<>();

    public String getEntityTag() {
        return entityTag;
    }

    private DamageManager damageManager;
    private AttributeManager attributesConfig;
    private final AbilityManager<NoctisEntity> abilityManager = new AbilityManager<>();
    private final EffectManager effectManager = new EffectManager();
    private EntityConfig config;
    private final String entityTag;


    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected NoctisEntity(EntityType<? extends Monster> type, Level level, String tag, AttributeConfig attributes, EntityConfig config) {
        super(type, level);
        this.entityTag = tag;
        this.attributesConfig = new AttributeManager(attributes);
        this.config = config;
        this.xpReward = config.xpReward;
        this.damageManager = new DamageManager();
    }

    // =============================
    // 📊 DADOS SINCRONIZADOS
    // =============================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_ID, 0);
    }

    public final int getSkinId() {
        return this.entityData.get(SKIN_ID);
    }

    protected final void setSkinId(int id) {
        if (id >= 0 && id < getSkinCount()) {
            this.entityData.set(SKIN_ID, id);
        }
    }

    // =============================
    // 🎬 ANIMAÇÕES (GECKOLIB)
    // =============================

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar register) {
        register.add(new AnimationController<>(this, "movement", 8, this::movementController));
        register.add(new AnimationController<>(this, "attack", 0, this::attackController));
    }

    protected <E extends NoctisEntity> PlayState movementController(final AnimationState<E> event) {
        if (event.isMoving() && this.getDeltaMovement().lengthSqr() > 0.002) {
            return event.setAndContinue(WALK_ANIM);
        }
        return event.setAndContinue(IDLE_ANIM);
    }

    protected PlayState attackController(AnimationState<?> event) {
        var ability = this.getAbilityManager().getCurrentAbility();
        if (ability != null && ability.getAnimation() != null) {
            return event.setAndContinue(ability.getAnimation());
        }
        return attackHandler.handle((AnimationState<NoctisEntity>) event, this);
    }

    // =============================
    // 🧠 OBJETIVOS DE IA
    // =============================

    @Override
    protected void registerGoals() {
        this.registerMovementGoals();
        this.registerCombatGoals();
        this.registerLookingGoals();
        this.registerTargetGoals();
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

    // =============================
    // 🔄 TICK & PERSISTÊNCIA
    // =============================

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            abilityManager.tick(this);
            effectManager.tick(this);
        }

        // efeito de transição visual
        boolean isDamaged = this.getVisualState() == VisualState.DAMAGED;

        if (isDamaged && !wasDamaged) {
            triggerDamageTransitionEffects();
        }

        wasDamaged = isDamaged;
    }

    private void triggerDamageTransitionEffects() {
        if (this.level().isClientSide) return;

        this.level().playSound(
                null,
                this.blockPosition(),
                net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                net.minecraft.sounds.SoundSource.HOSTILE,
                1.0f,
                0.6f
        );

        // partículas simples
        ((ServerLevel) this.level()).sendParticles(
                net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                this.getX(), this.getY(0.5), this.getZ(),
                8,
                0.3, 0.3, 0.3,
                0.1
        );
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData,
                                        CompoundTag dataTag) {
        setSkinId(random.nextInt(getSkinCount()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.getSkinId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId")) {
            this.setSkinId(tag.getInt("SkinId"));
        }
    }

    // =============================
    // 💥 DANO DIRECIONADO
    // =============================

    @Override
    public boolean hurt(DamageSource source, float amount) {

        DamageConfig ctx = new DamageConfig(source);
        if (!damageManager.canBeDamaged(ctx)) {
            return false;
        }

        amount = damageManager.applyModifiers(ctx, amount);

        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {

        if (!this.getDamageProfile().shouldTakeFallDamage()) {
            return false; // cancela dano
        }

        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    public VisualState getVisualState() {
        float hp = this.getHealth();
        float max = this.getMaxHealth();

        float threshold = this.config.damageThreshold;
        // você vai adicionar isso no EntityConfig

        if (hp / max <= threshold) {
            return VisualState.DAMAGED;
        }

        return VisualState.NORMAL;
    }
    // =============================
    // 🔗 MÉTODOS ABSTRATOS
    // =============================

    protected int getSkinCount() {
        return this.config.skinCount;
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
}
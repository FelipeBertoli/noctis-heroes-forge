package com.noctisheroes.entity;

import com.noctisheroes.common.managers.AbilityManager;
import com.noctisheroes.common.combat.damage.DamageContext;
import com.noctisheroes.common.managers.DamageManager;
import com.noctisheroes.entity.handlers.MeleeAttackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

/**
 * Classe base aprimorada para todas as entities do Noctis.
 *
 * ✅ Melhorias:
 * - Encapsulamento privado de dados sensíveis
 * - Métodos template para customização em subclasses
 * - Cache de animações e dados sincronizados
 * - Lazy initialization para otimização de memória
 * - Detection range e follow range configuráveis
 * - Dano direcionado (pode ser bloqueado para certas entities)
 */
public abstract class NoctisEntity extends Monster implements GeoEntity {
    // =============================
    // 📦 CONSTANTES & CACHE
    // =============================

    private DamageManager damageManager;
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.viltrumite.walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.viltrumite.idle");

    public static final RawAnimation RIGHT_ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.viltrumite.right_attack");
    public static final RawAnimation LEFT_ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.viltrumite.left_attack");

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

    private final String entityTag;
    private final AbilityManager<NoctisEntity> abilityManager = new AbilityManager<>();

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected NoctisEntity(EntityType<? extends Monster> entityType, Level level, String entityTag) {
        super(entityType, level);
        this.entityTag = entityTag;
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
        }
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

        DamageContext ctx = new DamageContext(source);
        if (!damageManager.canBeDamaged(ctx)) {
            return false;
        }

        amount = damageManager.applyModifiers(ctx, amount);

        return super.hurt(source, amount);
    }

    // =============================
    // 🔗 MÉTODOS ABSTRATOS
    // =============================

    protected abstract int getSkinCount();

    public DamageManager getDamageProfile() {
        return damageManager;
    }

    public AbilityManager<NoctisEntity> getAbilityManager() {
        return abilityManager;
    }
}
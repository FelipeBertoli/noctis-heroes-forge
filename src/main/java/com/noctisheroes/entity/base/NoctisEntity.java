package com.noctisheroes.entity.base;

import com.noctisheroes.entity.base.handlers.MeleeAttackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
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
import software.bernie.geckolib.core.animatable.GeoAnimatable;
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
 * - Métodos privados para operações internas
 */
public abstract class NoctisEntity extends Monster implements GeoEntity {
    // =============================
    // 📦 CONSTANTES & CACHE
    // =============================

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
    private final String entityTag;

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected NoctisEntity(EntityType<? extends Monster> entityType, Level level, String entityTag) {
        super(entityType, level);
        this.entityTag = entityTag;
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
        register.add(new AnimationController<>(this, "walkController", 8, this::walkController));
        register.add(new AnimationController<>(this, "attackController", 0, this::attackController));
    }

    /**
     * Template method para controle de animação de caminhada.
     * Subclasses podem fazer override para comportamento customizado.
     */
    protected <E extends NoctisEntity> PlayState walkController(final AnimationState<E> event) {
        if (event.isMoving() && this.getDeltaMovement().lengthSqr() > 0.002) {
            return event.setAndContinue(WALK_ANIM);
        }
        return event.setAndContinue(IDLE_ANIM);
    }

    private PlayState attackController(final AnimationState<?> event) {
        return attackHandler.handle((AnimationState<NoctisEntity>) event, this);
    }

    // =============================
    // 🧠 OBJETIVOS DE IA
    // =============================

    @Override
    protected void registerGoals() {
        // Objetivos de movimento
        this.registerMovementGoals();

        // Objetivos de combate
        this.registerCombatGoals();

        // Objetivos de interação
        this.registerLookingGoals();

        // Objetivos de alvo
        this.registerTargetGoals();
    }

    /**
     * Registra objetivos de movimento. Override para customização.
     */
    protected void registerMovementGoals() {
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    /**
     * Registra objetivos de combate. Override para customização.
     */
    protected void registerCombatGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
    }

    /**
     * Registra objetivos de olhar/interação visual.
     */
    protected void registerLookingGoals() {
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    /**
     * Registra objetivos de alvo. Override para adicionar mais alvos.
     */
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
    // 🔗 MÉTODOS ABSTRATOS
    // =============================

    /**
     * Retorna a quantidade de skins disponíveis para esta entidade.
     */
    protected abstract int getSkinCount();
}
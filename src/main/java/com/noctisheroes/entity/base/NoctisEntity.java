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
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class NoctisEntity extends Monster implements GeoEntity {
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.viltrumite.walk");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.viltrumite.idle");
    public static final RawAnimation RIGHT_ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.viltrumite.right_attack");
    public static final RawAnimation LEFT_ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.viltrumite.left_attack");


    public String getEntityTag() {
        return entityTag;
    }

    protected String entityTag;

    private final MeleeAttackHandler<NoctisEntity> attackHandler = new MeleeAttackHandler<>();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> SKIN_ID =
            SynchedEntityData.defineId(NoctisEntity.class, EntityDataSerializers.INT);
    protected abstract int getSkinCount();

    public NoctisEntity(EntityType<? extends Monster> entityType,
                        Level level,
                        String entityTag) {
        super(entityType, level);
        this.entityTag = entityTag;
    }

    protected <E extends NoctisEntity> PlayState walkController(final AnimationState<E> event) {
        if (event.isMoving() && this.getDeltaMovement().lengthSqr() > 0.002) {
            return event.setAndContinue(WALK_ANIM);
        }
        return event.setAndContinue(IDLE_ANIM);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar register) {
        register.add(new AnimationController<>(this, "walkController", 8, this::walkController));
        register.add(new AnimationController<>(this, "attackController", 0, this::attackController));
    }

    private PlayState attackController(final AnimationState<?> event) {
        return attackHandler.handle((AnimationState<NoctisEntity>) event, this);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_ID, 0);
    }

    public int getSkinId() {
        return entityData.get(SKIN_ID);
    }

    protected void setSkinId(int id) {
        entityData.set(SKIN_ID, id);
    }

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

    // 🔥 IMPORTANTE: Carregar a skin do NBT (ao voltar pro mundo)
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId")) {
            this.setSkinId(tag.getInt("SkinId"));
        }
    }

}
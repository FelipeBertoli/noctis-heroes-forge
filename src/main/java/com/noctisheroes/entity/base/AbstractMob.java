package com.noctisheroes.entity.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Classe base para todos os mobs do mod.
 * Implementa GeoEntity do Geckolib para modelos e animações via JSON.
 */
public abstract class AbstractMob extends Monster implements GeoEntity {

    // Cache de animação — uma instância por entidade, gerenciada pelo Geckolib
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> SKIN_ID =
            SynchedEntityData.defineId(AbstractMob.class, EntityDataSerializers.INT);

    // Animações padrão — defina os nomes iguais ao seu arquivo .animation.json
    protected static final RawAnimation ANIM_IDLE  = RawAnimation.begin().thenLoop("animation.mob.idle");
    protected static final RawAnimation ANIM_WALK  = RawAnimation.begin().thenLoop("animation.mob.walk");
    protected static final RawAnimation ANIM_FLY   = RawAnimation.begin().thenLoop("animation.mob.fly");
    protected static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlayAndHold("animation.mob.attack");

    protected AbstractMob(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // ── Geckolib ─────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "main", 4, state -> {
            // Lógica de animação padrão — sobrescreva em subclasses se necessário
            if (state.isMoving()) {
                return state.setAndContinue(ANIM_WALK);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }

    // ── Skin ─────────────────────────────────────────────────────────────────

    /** Quantas skins (variantes de textura) este mob possui. */
    protected abstract int getSkinCount();

    public int getSkinId() {
        return entityData.get(SKIN_ID);
    }


    protected void setSkinId(int id) {
        entityData.set(SKIN_ID, id);
    }

    // ── SynchedData ──────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN_ID, 0);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", getSkinId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSkinId(tag.getInt("SkinId"));
    }

    // ── Spawn ─────────────────────────────────────────────────────────────────

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData,
                                        CompoundTag dataTag) {
        setSkinId(random.nextInt(getSkinCount()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier,
                                   net.minecraft.world.damagesource.DamageSource source) {

        return false;
    }

    protected abstract Vec3 getFluidFallVector();
}
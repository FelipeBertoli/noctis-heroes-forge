package com.noctisheroes.client.model;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.base.AbstractMob;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modelo base para todos os mobs do mod.
 * Resolve automaticamente os caminhos JSON pelo nome do mob:
 *
 *   geo/entity/<mobName>.geo.json
 *   animations/<mobName>.animation.json
 *
 * A textura é resolvida pelo renderer (suporte a multi-skin).
 */
public abstract class AbstractMobModel<T extends AbstractMob> extends GeoModel<T> {

    private final ResourceLocation modelPath;
    private final ResourceLocation animationPath;
    private final ResourceLocation texturePath;

    protected AbstractMobModel(String mobName) {
        this.modelPath       = new ResourceLocation(NoctisHeroes.MODID, "geo/entity/" + mobName + ".geo.json");
        this.animationPath   = new ResourceLocation(NoctisHeroes.MODID, "animations/" + mobName + ".animation.json");
        this.texturePath = new ResourceLocation(NoctisHeroes.MODID, "textures/entity/" + mobName + "/" + mobName + "_0.png");
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return modelPath;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return texturePath;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return animationPath;
    }
}
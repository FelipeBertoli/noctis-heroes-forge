package com.noctisheroes.client;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NoctisEntityModel<T extends NoctisEntity> extends GeoModel<T> {

    private final ResourceLocation modelPath;
    private final ResourceLocation animationPath;
    private final ResourceLocation texturePath;

    public NoctisEntityModel(String modelTag, String typeTag, String textureTag) {
        this.modelPath = new ResourceLocation(NoctisHeroes.MODID, "geo/entity/" + modelTag + ".geo.json");
        this.animationPath = new ResourceLocation(NoctisHeroes.MODID, "animations/" + modelTag + ".animation.json");
        this.texturePath = new ResourceLocation(NoctisHeroes.MODID, "textures/entity/" + typeTag + "/" + textureTag + "_0.png");
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
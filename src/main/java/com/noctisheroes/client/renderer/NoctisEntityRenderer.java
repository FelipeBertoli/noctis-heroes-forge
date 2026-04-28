package com.noctisheroes.client.renderer;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.base.NoctisEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public abstract class NoctisEntityRenderer<T extends NoctisEntity>
        extends GeoEntityRenderer<T> {

    private final String mobName;
    private final String mobTag;

    protected NoctisEntityRenderer(EntityRendererProvider.Context context,
                                   software.bernie.geckolib.model.GeoModel<T> model,
                                   String mobName, String mobTag) {
        super(context, model);
        this.mobName = mobName;
        this.mobTag = mobTag;
        this.shadowRadius=0.6f;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        int skinId = entity.getSkinId();
        return new ResourceLocation(NoctisHeroes.MODID,
                "textures/entity/" + mobTag + "/" + mobName + "_" + skinId + ".png");
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }


}
package com.noctisheroes.client.renderer;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.base.AbstractMob;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer base para todos os mobs do mod.
 * Usa Geckolib — o modelo e animações vêm de JSON, não de Java.
 *
 * Subclasses só precisam informar o nome do mob para montar os caminhos.
 * As texturas de skin seguem o padrão:
 *   textures/entity/<mobName>/<mobName>_<skinId>.png
 */
public abstract class AbstractMobRenderer<T extends AbstractMob>
        extends GeoEntityRenderer<T> {

    private final String mobName;

    protected AbstractMobRenderer(EntityRendererProvider.Context context,
                                  software.bernie.geckolib.model.GeoModel<T> model,
                                  String mobName) {
        super(context, model);
        this.mobName = mobName;
        this.shadowRadius=0.6f;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        int skinId = entity.getSkinId();
        return new ResourceLocation(NoctisHeroes.MODID,
                "textures/entity/" + mobName + "/" + mobName + "_" + skinId + ".png");
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }


}
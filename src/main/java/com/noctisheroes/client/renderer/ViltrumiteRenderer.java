package com.noctisheroes.client.renderer;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.client.model.ViltrumiteModel;
import com.noctisheroes.entity.ViltrumiteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ViltrumiteRenderer extends MobRenderer<ViltrumiteEntity, ViltrumiteModel<ViltrumiteEntity>> {

    // Array com os caminhos das 3 texturas
    // Coloque seus arquivos .png em:
    // src/main/resources/assets/noctisheroes/textures/entity/viltrumite/
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation(NoctisHeroes.MODID, "textures/entity/viltrumite/viltrumite_0.png"),
            new ResourceLocation(NoctisHeroes.MODID, "textures/entity/viltrumite/viltrumite_1.png"),
            new ResourceLocation(NoctisHeroes.MODID, "textures/entity/viltrumite/viltrumite_2.png")
    };

    public ViltrumiteRenderer(EntityRendererProvider.Context context) {
        super(context, new ViltrumiteModel<>(context.bakeLayer(ViltrumiteModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ViltrumiteEntity entity) {
        // Usa o skinId sincronizado para escolher a textura correta
        int skinId = entity.getSkinId();
        // Garante que o índice está dentro dos limites
        if (skinId < 0 || skinId >= TEXTURES.length) {
            skinId = 0;
        }
        return TEXTURES[skinId];
    }
}
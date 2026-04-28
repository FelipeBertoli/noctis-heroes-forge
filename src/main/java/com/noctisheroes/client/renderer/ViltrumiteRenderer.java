package com.noctisheroes.client.renderer;

import com.noctisheroes.client.model.ViltrumiteModel;
import com.noctisheroes.entity.mobs.ViltrumiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer do Viltrumita.
 * Apenas informa o nome — toda a lógica fica em AbstractMobRenderer.
 */
public class ViltrumiteRenderer extends NoctisEntityRenderer<ViltrumiteEntity> {

    public ViltrumiteRenderer(EntityRendererProvider.Context context) {
        super(context, new ViltrumiteModel(), "viltrumite", "viltrumite");
    }
}
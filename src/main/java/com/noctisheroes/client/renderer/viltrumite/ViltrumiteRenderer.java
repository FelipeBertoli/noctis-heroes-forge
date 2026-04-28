package com.noctisheroes.client.renderer.viltrumite;

import com.noctisheroes.client.model.viltrumite.ViltrumiteModel;
import com.noctisheroes.client.renderer.NoctisEntityRenderer;
import com.noctisheroes.entity.mobs.viltrumite.ViltrumiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer para Viltrumita comum.
 */
public class ViltrumiteRenderer extends NoctisEntityRenderer<ViltrumiteEntity> {

    public ViltrumiteRenderer(EntityRendererProvider.Context context) {
        super(context, new ViltrumiteModel(), "viltrumite", "viltrumite", 6);
    }
}
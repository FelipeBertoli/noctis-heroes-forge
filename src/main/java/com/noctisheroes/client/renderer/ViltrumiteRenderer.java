package com.noctisheroes.client.renderer;

import com.noctisheroes.client.model.ViltrumiteModel;
import com.noctisheroes.entity.ViltrumiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer do Viltrumita.
 * Apenas informa o nome — toda a lógica fica em AbstractMobRenderer.
 */
public class ViltrumiteRenderer extends AbstractMobRenderer<ViltrumiteEntity> {

    public ViltrumiteRenderer(EntityRendererProvider.Context context) {
        super(context, new ViltrumiteModel(), "viltrumite");
    }
}
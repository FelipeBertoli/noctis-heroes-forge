package com.noctisheroes.client.renderer;

import com.noctisheroes.client.model.ThraggBossModel;
import com.noctisheroes.entity.boss.viltrumite.ThraggBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer do Viltrumita.
 * Apenas informa o nome — toda a lógica fica em AbstractMobRenderer.
 */
public class ThraggBossRenderer extends NoctisEntityRenderer<ThraggBoss> {

    public ThraggBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ThraggBossModel(), "thragg_boss", "viltrumite");
    }
}
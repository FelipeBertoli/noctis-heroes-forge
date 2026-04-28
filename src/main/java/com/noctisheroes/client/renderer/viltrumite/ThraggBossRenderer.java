package com.noctisheroes.client.renderer.viltrumite;

import com.noctisheroes.client.model.viltrumite.ThraggBossModel;
import com.noctisheroes.client.renderer.NoctisEntityRenderer;
import com.noctisheroes.entity.mobs.viltrumite.ThraggBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer para Boss Thragg.
 */
public class ThraggBossRenderer extends NoctisEntityRenderer<ThraggBoss> {

    public ThraggBossRenderer(EntityRendererProvider.Context context) {
        super(context, new ThraggBossModel(), "thragg_boss", "viltrumite",1);
    }
}
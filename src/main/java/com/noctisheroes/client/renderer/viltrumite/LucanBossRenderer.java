package com.noctisheroes.client.renderer.viltrumite;

import com.noctisheroes.client.model.viltrumite.LucanBossModel;
import com.noctisheroes.client.renderer.NoctisEntityRenderer;
import com.noctisheroes.entity.mobs.viltrumite.LucanBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer para Boss Lucan.
 */
public class LucanBossRenderer extends NoctisEntityRenderer<LucanBoss> {

    public LucanBossRenderer(EntityRendererProvider.Context context) {
        super(context, new LucanBossModel(), "lucan_boss", "viltrumite",1);
    }
}
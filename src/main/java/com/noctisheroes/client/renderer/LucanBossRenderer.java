package com.noctisheroes.client.renderer;

import com.noctisheroes.client.model.LucanBossModel;
import com.noctisheroes.client.model.ThraggBossModel;
import com.noctisheroes.entity.boss.viltrumite.LucanBoss;
import com.noctisheroes.entity.boss.viltrumite.ThraggBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renderer do Viltrumita.
 * Apenas informa o nome — toda a lógica fica em AbstractMobRenderer.
 */
public class LucanBossRenderer extends NoctisEntityRenderer<LucanBoss> {

    public LucanBossRenderer(EntityRendererProvider.Context context) {
        super(context, new LucanBossModel(), "lucan_boss", "viltrumite");
    }
}
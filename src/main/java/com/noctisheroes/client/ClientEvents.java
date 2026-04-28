package com.noctisheroes.client;

import com.noctisheroes.client.renderer.LucanBossRenderer;
import com.noctisheroes.client.renderer.ThraggBossRenderer;
import com.noctisheroes.client.renderer.ViltrumiteRenderer;
import com.noctisheroes.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Com Geckolib não há mais registerLayerDefinitions.
 * Só registra o renderer — modelo e animações vêm dos JSONs.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VILTRUMITE.get(), ViltrumiteRenderer::new);
        event.registerEntityRenderer(ModEntities.THRAGG_BOSS.get(), ThraggBossRenderer::new);
        event.registerEntityRenderer(ModEntities.LUCAN_BOSS.get(), LucanBossRenderer::new);
        // Adicione novos mobs aqui — uma linha por mob
    }
}
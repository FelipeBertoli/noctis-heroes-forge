package com.noctisheroes.client;

import com.noctisheroes.client.model.ViltrumiteModel;
import com.noctisheroes.client.renderer.ViltrumiteRenderer;
import com.noctisheroes.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    // Registra a camada do modelo (deve ser feito antes do renderer)
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ViltrumiteModel.LAYER_LOCATION,
                ViltrumiteModel::createBodyLayer
        );
    }

    // Registra o renderer da entidade
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.VILTRUMITE.get(),
                ViltrumiteRenderer::new
        );
    }
}
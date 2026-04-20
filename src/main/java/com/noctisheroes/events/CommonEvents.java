package com.noctisheroes.events;

import com.noctisheroes.entity.ViltrumiteEntity;
import com.noctisheroes.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.VILTRUMITE.get(), ViltrumiteEntity.createAttributes().build());
    }
}
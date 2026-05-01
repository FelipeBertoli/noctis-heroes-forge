package com.noctisheroes;

import com.noctisheroes.registry.EntitiesRegistry;
import com.noctisheroes.registry.ItemsRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NoctisHeroes.MODID)
public class NoctisHeroes {
    public static final String MODID = "noctisheroes";

    public NoctisHeroes() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemsRegistry.ITEMS.register(bus);
        EntitiesRegistry.register(bus);

    }
}
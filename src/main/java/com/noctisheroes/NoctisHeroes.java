package com.noctisheroes;

import com.noctisheroes.registry.ModEntities;
import com.noctisheroes.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NoctisHeroes.MODID)
public class NoctisHeroes {
    public static final String MODID = "noctisheroes";

    public NoctisHeroes() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(bus);
        ModEntities.register(bus);

    }
}
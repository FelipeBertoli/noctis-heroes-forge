package com.noctisheroes.registry;

import com.noctisheroes.NoctisHeroes;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NoctisHeroes.MODID);

    // Cores do ovo: cor de fundo (0x1a1a2e = azul escuro) e cor das pintas (0xc0392b = vermelho)
    // Troque pelos valores hexadecimais que preferir
    public static final RegistryObject<ForgeSpawnEggItem> VILTRUMITE_SPAWN_EGG =
            ITEMS.register("viltrumite_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            ModEntities.VILTRUMITE,
                            0x1a1a2e,  // cor de fundo
                            0xc0392b,  // cor das pintas
                            new net.minecraft.world.item.Item.Properties()
                    )
            );
}
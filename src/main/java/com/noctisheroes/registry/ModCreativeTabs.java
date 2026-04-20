package com.noctisheroes.registry;

import com.noctisheroes.NoctisHeroes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NoctisHeroes.MODID);

    public static final RegistryObject<CreativeModeTab> NOCTIS_TAB =
            CREATIVE_TABS.register("noctis_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.noctisheroes.noctis_tab"))
                            .icon(() -> new ItemStack(ModItems.VILTRUMITE_SPAWN_EGG.get()))
                            .displayItems((params, output) -> {
                                output.accept(ModItems.VILTRUMITE_SPAWN_EGG.get());
                            })
                            .build()
            );
}
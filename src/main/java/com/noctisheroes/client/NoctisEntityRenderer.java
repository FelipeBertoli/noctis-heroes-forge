package com.noctisheroes.client;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.states.VisualState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.HashMap;
import java.util.Map;

public class NoctisEntityRenderer<T extends NoctisEntity>
        extends GeoEntityRenderer<T> {

    // =============================
    // 📦 CAMPOS
    // =============================

    private final String texturePath;
    private final String classPath;
    private final int skinCount;

    /**
     * Cache agora suporta variação NORMAL / DAMAGED por skin
     * key = "skinId_state"
     */
    private final Map<String, ResourceLocation> textureCacheMap = new HashMap<>();

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    public NoctisEntityRenderer(EntityRendererProvider.Context context,
                                String modelTag,
                                String texturePath,
                                String classPath,
                                int skinCount) {

        super(context, new NoctisEntityModel<>(modelTag, classPath, texturePath));

        this.texturePath = texturePath;
        this.classPath = classPath;
        this.skinCount = skinCount;

        this.shadowRadius = 0.6f;
    }

    // =============================
    // 🎨 TEXTURAS
    // =============================

    @Override
    public ResourceLocation getTextureLocation(T entity) {

        int skinId = entity.getSkinId();

        if (skinId < 0 || skinId >= this.skinCount) {
            skinId = 0;
        }

        // estado visual (NORMAL / DAMAGED)
        boolean damaged = entity.getVisualState() == VisualState.DAMAGED;

        String stateSuffix = damaged ? "_damaged" : "";
        String key = skinId + stateSuffix;

        // cache hit
        if (textureCacheMap.containsKey(key)) {
            return textureCacheMap.get(key);
        }

        // monta path
        ResourceLocation texture = new ResourceLocation(
                NoctisHeroes.MODID,
                "textures/entity/" +
                        this.classPath + "/" +
                        this.texturePath + "_" +
                        skinId + stateSuffix + ".png"
        );

        textureCacheMap.put(key, texture);
        return texture;
    }

    // =============================
    // RENDER TYPE
    // =============================

    @Override
    public RenderType getRenderType(T animatable,
                                    ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource,
                                    float partialTick) {

        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}
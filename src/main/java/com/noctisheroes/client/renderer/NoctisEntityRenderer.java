package com.noctisheroes.client.renderer;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.base.NoctisEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.model.GeoModel;

/**
 * Renderer base para todas as entities Noctis.
 *
 * ✅ Melhorias:
 * - Configuração via constructor para máxima flexibilidade
 * - Caching de texture locations
 * - Encapsulamento completo
 * - Suporte a múltiplas skins eficiente
 */
public abstract class NoctisEntityRenderer<T extends NoctisEntity>
        extends GeoEntityRenderer<T> {

    // =============================
    // 📦 CAMPOS PRIVADOS
    // =============================

    private final String texturePath;
    private final String classPath;
    private final ResourceLocation[] textureCache;
    private final int skinCount;

    // =============================
    // 🏗️ CONSTRUTOR GENÉRICO
    // =============================

    protected NoctisEntityRenderer(EntityRendererProvider.Context context,
                                   GeoModel<T> model,
                                   String texturePath,
                                   String classPath,
                                   int skinCount) {
        super(context, model);
        this.texturePath = texturePath;
        this.classPath = classPath;
        this.skinCount = skinCount;
        this.textureCache = new ResourceLocation[skinCount];
        this.shadowRadius = 0.6f;
    }

    // =============================
    // 🎨 TEXTURAS
    // =============================

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        int skinId = entity.getSkinId();

        // Validação e bounds check
        if (skinId < 0 || skinId >= this.skinCount) {
            skinId = 0; // Fallback para skin padrão
        }

        // Cache hit
        if (this.textureCache[skinId] != null) {
            return this.textureCache[skinId];
        }

        // Cache miss - cria e armazena
        ResourceLocation texture = new ResourceLocation(NoctisHeroes.MODID,
                "textures/entity/" + this.classPath + "/" + this.texturePath + "_" + skinId + ".png");

        this.textureCache[skinId] = texture;
        return texture;
    }

    // =============================
    // 🎬 RENDERIZAÇÃO
    // =============================

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource,
                                    float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    // =============================
    // 📊 GETTERS
    // =============================

    /**
     * Retorna o caminho de textura configurado.
     */
    protected String getTexturePath() {
        return this.texturePath;
    }

    /**
     * Retorna a quantidade de skins em cache.
     */
    protected int getSkinCount() {
        return this.skinCount;
    }

    /**
     * Limpa o cache de texturas (deve ser chamado no client unload se necessário).
     */
    protected void clearTextureCache() {
        for (int i = 0; i < this.textureCache.length; i++) {
            this.textureCache[i] = null;
        }
    }
}
package xyz.brassgoggledcoders.mccivilizations.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import xyz.brassgoggledcoders.mccivilizations.item.CivilizationBannerBlockItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class CivilizationBannerItemStackRender extends BlockEntityWithoutLevelRenderer {
    public static final BlockEntityWithoutLevelRenderer INSTANCE = new CivilizationBannerItemStackRender(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels()
    );

    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;

    public CivilizationBannerItemStackRender(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
        ModelPart modelPart = pEntityModelSet.bakeLayer(ModelLayers.BANNER);
        this.flag = modelPart.getChild("flag");
        this.pole = modelPart.getChild("pole");
        this.bar = modelPart.getChild("bar");
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack,
                             MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = this.getPatterns(pStack);
        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 0.5D, 0.5D);
        this.pole.visible = true;
        pPoseStack.pushPose();
        pPoseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer vertexconsumer = ModelBakery.BANNER_BASE.buffer(pBuffer, RenderType::entitySolid);
        this.pole.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
        this.bar.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
        BlockPos blockpos = BlockPos.ZERO;
        float f2 = ((float) Math.floorMod(blockpos.getX() * 7L + blockpos.getY() * 9L + blockpos.getZ() * 13L, 100L)) / 100.0F;
        this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(((float) Math.PI * 2F) * f2)) * (float) Math.PI;
        this.flag.y = -32.0F;
        BannerRenderer.renderPatterns(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, this.flag, ModelBakery.BANNER_BASE, true, patterns);
        pPoseStack.popPose();
        pPoseStack.popPose();
    }

    private List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns(ItemStack itemStack) {
        if (itemStack.getItem() instanceof CivilizationBannerBlockItem bannerBlockItem) {
            if (Minecraft.getInstance().player != null) {
                return bannerBlockItem.getPatterns(Minecraft.getInstance().player, itemStack);
            }

        }

        return List.of();
    }

}

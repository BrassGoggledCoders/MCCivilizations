package xyz.brassgoggledcoders.mccivilizations.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.block.HangingCivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.block.StandingCivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;

import java.util.List;

public class CivilizationBannerBlockEntityRenderer implements BlockEntityRenderer<CivilizationBannerBlockEntity> {
    private final ModelPart flag;
    private final ModelPart pole;
    private final ModelPart bar;

    public CivilizationBannerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.BANNER);
        this.flag = modelpart.getChild("flag");
        this.pole = modelpart.getChild("pole");
        this.bar = modelpart.getChild("bar");
    }

    @Override
    public void render(CivilizationBannerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        List<Pair<Holder<BannerPattern>, DyeColor>> list = pBlockEntity.getPatterns();
        boolean flag = pBlockEntity.getLevel() == null;
        pPoseStack.pushPose();
        long i;
        if (flag) {
            i = 0L;
            pPoseStack.translate(0.5D, 0.5D, 0.5D);
            this.pole.visible = true;
        } else {
            i = pBlockEntity.getLevel().getGameTime();
            BlockState blockstate = pBlockEntity.getBlockState();
            if (blockstate.getBlock() instanceof StandingCivilizationBannerBlock) {
                pPoseStack.translate(0.5D, 0.5D, 0.5D);
                float f1 = (float) (-blockstate.getValue(StandingCivilizationBannerBlock.ROTATION) * 360) / 16.0F;
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f1));
                this.pole.visible = true;
            } else {
                pPoseStack.translate(0.5D, -0.16666667F, 0.5D);
                float f3 = -blockstate.getValue(HangingCivilizationBannerBlock.FACING).toYRot();
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f3));
                pPoseStack.translate(0.0D, -0.3125D, -0.4375D);
                this.pole.visible = false;
            }
        }

        pPoseStack.pushPose();
        pPoseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer vertexconsumer = ModelBakery.BANNER_BASE.buffer(pBufferSource, RenderType::entitySolid);
        this.pole.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
        this.bar.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay);
        BlockPos blockpos = pBlockEntity.getBlockPos();
        float f2 = ((float) Math.floorMod(blockpos.getX() * 7L + blockpos.getY() * 9L + blockpos.getZ() * 13L + i, 100L) + pPartialTick) / 100.0F;
        this.flag.xRot = (-0.0125F + 0.01F * Mth.cos(((float) Math.PI * 2F) * f2)) * (float) Math.PI;
        this.flag.y = -32.0F;
        BannerRenderer.renderPatterns(pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, this.flag, ModelBakery.BANNER_BASE, true, list);
        pPoseStack.popPose();
        pPoseStack.popPose();
    }
}

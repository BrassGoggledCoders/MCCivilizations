package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class StandingCivilizationBannerBlock extends AbstractCivilizationBannerBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public StandingCivilizationBannerBlock(CivilizationBannerType bannerType, Properties pProperties) {
        super(bannerType, pProperties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(@NotNull BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).getMaterial().isSolid();
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(ROTATION, Mth.floor((double) ((180.0F + pContext.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15);
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() :
                super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(ROTATION, pRotation.rotate(pState.getValue(ROTATION), 16));
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.setValue(ROTATION, pMirror.mirror(pState.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ROTATION);
    }
}

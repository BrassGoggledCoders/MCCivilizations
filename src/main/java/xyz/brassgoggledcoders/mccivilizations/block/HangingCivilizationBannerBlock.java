package xyz.brassgoggledcoders.mccivilizations.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

public class HangingCivilizationBannerBlock extends AbstractCivilizationBannerBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(0.0D, 0.0D, 14.0D, 16.0D, 12.5D, 16.0D), Direction.SOUTH, Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.5D, 2.0D), Direction.WEST, Block.box(14.0D, 0.0D, 0.0D, 16.0D, 12.5D, 16.0D), Direction.EAST, Block.box(0.0D, 0.0D, 0.0D, 2.0D, 12.5D, 16.0D)));

    public HangingCivilizationBannerBlock(CivilizationBannerType bannerType, Properties pProperties) {
        super(bannerType, pProperties);
    }

    @Override
    @NotNull
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).getMaterial().isSolid();
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == pState.getValue(FACING).getOpposite() && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPES.get(pState.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Direction[] adirection = pContext.getNearestLookingDirections();

        for (Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }

        return null;
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }
}

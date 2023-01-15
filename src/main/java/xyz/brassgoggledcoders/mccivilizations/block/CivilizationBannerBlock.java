package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationServices;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

public class CivilizationBannerBlock extends AbstractBannerBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    private final CivilizationBannerType bannerType;

    public CivilizationBannerBlock(CivilizationBannerType bannerType, DyeColor pColor, Properties pProperties) {
        super(pColor, pProperties.randomTicks());
        this.bannerType = bannerType;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof Player player && pLevel.getBlockEntity(pPos) instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            ICivilizationRepository civilizations = CivilizationServices.getCivilizationService(pLevel);
            Civilization playerCivilization = civilizations.getCivilizationByCitizen(player);
            if (playerCivilization != null) {
                bannerBlockEntity.setCivilizationUUID(playerCivilization.getId());
            } else if (bannerType == CivilizationBannerType.CAPITAL) {
                Component name = MCCivilizationsText.NO_NAME_CIVILIZATION;
                if (pStack.hasCustomHoverName()) {
                    name = pStack.getHoverName();
                }

                Civilization newCivilization = new Civilization(
                        UUID.randomUUID(),
                        name,
                        pStack
                );
                civilizations.upsertCivilization(newCivilization);
                civilizations.joinCivilization(newCivilization, player);
                bannerBlockEntity.setCivilizationUUID(newCivilization.getId());
            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CivilizationBannerBlockEntity(pPos, pState);
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
        ICivilizationRepository civilizations = CivilizationServices.getCivilizationService(pContext.getLevel());
        if (pContext.getPlayer() != null) {
            Civilization playerCivilization = civilizations.getCivilizationByCitizen(pContext.getPlayer());
            if ((playerCivilization == null) == (this.bannerType == CivilizationBannerType.CAPITAL)) {
                return this.defaultBlockState().setValue(ROTATION, Mth.floor((double) ((180.0F + pContext.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15);
            }
        }

        return null;
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
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

    @Override
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.randomTick(pState, pLevel, pPos, pRandom);
        if (pLevel.getBlockEntity(pPos) instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            bannerBlockEntity.checkRefresh();
        }
    }
}

package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

public abstract class AbstractCivilizationBannerBlock extends Block implements EntityBlock {
    private final CivilizationBannerType bannerType;

    protected AbstractCivilizationBannerBlock(CivilizationBannerType bannerType, Properties pProperties) {
        super(pProperties.randomTicks());
        this.bannerType = bannerType;
    }

    public CivilizationBannerType getBannerType() {
        return bannerType;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new CivilizationBannerBlockEntity(pPos, pState);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof ServerPlayer player && pLevel.getBlockEntity(pPos) instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            ICivilizationRepository civilizations = CivilizationRepositories.getCivilizationRepository();
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
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.randomTick(pState, pLevel, pPos, pRandom);
        if (pLevel.getBlockEntity(pPos) instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            bannerBlockEntity.checkRefresh(true);
        }
    }
}

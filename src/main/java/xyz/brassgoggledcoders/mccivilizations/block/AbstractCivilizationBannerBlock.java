package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsLocationTypes;
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

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            if (this.bannerType == CivilizationBannerType.CAPITAL) {
                return bannerBlockEntity.handleCapitalBanner(pPlayer, pPlayer.getItemInHand(pHand));
            } else if (this.getBannerType() == CivilizationBannerType.CITY) {
                return bannerBlockEntity.handleCityBanner(pPlayer, pPlayer.getItemInHand(pHand));
            }

        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
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
            Component name = MCCivilizationsText.NO_NAME_CIVILIZATION;
            if (pStack.hasCustomHoverName()) {
                name = pStack.getHoverName();
            }
            if (playerCivilization != null) {
                bannerBlockEntity.setCivilizationUUID(playerCivilization.getId());
                if (this.getBannerType() == CivilizationBannerType.CITY) {
                    Location city = new Location(
                            UUID.randomUUID(),
                            GlobalPos.of(
                                    pLevel.dimension(),
                                    pPos
                            ),
                            MCCivilizationsLocationTypes.CITY.get(),
                            name
                    );
                    CivilizationRepositories.getLocationRepository()
                            .upsertLocation(playerCivilization, city);
                    bannerBlockEntity.setLocationUUID(city.getId());

                    CivilizationRepositories.getLandClaimRepository()
                            .addClaim(playerCivilization, pLevel.dimension(), new ChunkPos(pPos));

                    if (!pLevel.isClientSide()) {
                        player.sendSystemMessage(MCCivilizationsText.CITY_SETTLED);
                    }
                }
            } else if (bannerType == CivilizationBannerType.CAPITAL) {
                Civilization newCivilization = new Civilization(
                        UUID.randomUUID(),
                        name,
                        pStack,
                        DyeColor.WHITE
                );
                civilizations.upsertCivilization(newCivilization);
                civilizations.joinCivilization(newCivilization, player);
                bannerBlockEntity.setCivilizationUUID(newCivilization.getId());

                DyeColor dyeColor = bannerBlockEntity.getDyeColor();
                if (dyeColor != newCivilization.getDyeColor()) {
                    newCivilization.setDyeColor(dyeColor);
                    civilizations.upsertCivilization(newCivilization);
                }
                Location capital = new Location(
                        UUID.randomUUID(),
                        GlobalPos.of(
                                pLevel.dimension(),
                                pPos
                        ),
                        MCCivilizationsLocationTypes.CAPITAL.get(),
                        name
                );
                CivilizationRepositories.getLocationRepository()
                        .upsertLocation(newCivilization, capital);
                bannerBlockEntity.setLocationUUID(capital.getId());

                CivilizationRepositories.getLandClaimRepository()
                        .addClaim(newCivilization, pLevel.dimension(), new ChunkPos(pPos));
                if (!pLevel.isClientSide()) {
                    player.sendSystemMessage(MCCivilizationsText.CIVILIZATION_LEADING);
                }

            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pNewState.is(pState.getBlock()) && pLevel.getBlockEntity(pPos) instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            bannerBlockEntity.removeLocation();
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
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

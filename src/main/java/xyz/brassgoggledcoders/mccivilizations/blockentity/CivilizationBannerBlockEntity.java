package xyz.brassgoggledcoders.mccivilizations.blockentity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.block.AbstractCivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.block.CivilizationBannerType;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsBlocks;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CivilizationBannerBlockEntity extends BlockEntity implements Nameable {
    private UUID civilizationUUID;
    private UUID locationUUID;
    private Component name;
    private DyeColor dyeColor;
    @Nullable
    private ListTag itemPatterns;
    private List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

    public CivilizationBannerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        this(MCCivilizationsBlocks.CIVILIZATION_BANNER_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public CivilizationBannerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pPos, BlockState pBlockState) {
        super(blockEntityType, pPos, pBlockState);
        this.dyeColor = DyeColor.WHITE;
    }

    public void setCivilizationUUID(UUID uuid) {
        this.civilizationUUID = uuid;
        Civilization civilization = this.getCivilization();
        if (civilization == null) {
            this.civilizationUUID = null;
        } else {
            this.fromItem(civilization.getBanner());
        }
        this.setChanged();
    }

    public void setLocationUUID(UUID uuid) {
        this.locationUUID = uuid;
        Location location = this.getLocation();
        if (location == null) {
            this.locationUUID = null;
        } else {
            this.name = null;
        }
        this.setChanged();
    }

    public void fromItem(ItemStack pItem) {
        CompoundTag blockEntityData = BlockItem.getBlockEntityData(pItem);
        ListTag patternListTag = null;
        if (blockEntityData != null) {
            patternListTag = blockEntityData.getList("Patterns", Tag.TAG_COMPOUND)
                    .copy();
            this.dyeColor = DyeColor.byId(blockEntityData.getInt("Base"));
        } else {
            this.dyeColor = DyeColor.WHITE;
        }
        this.itemPatterns = patternListTag;
        this.patterns = null;
        this.setChanged();
    }

    @Nullable
    public Civilization getCivilization() {
        if (this.civilizationUUID != null) {
            Civilization civilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationById(this.civilizationUUID);
            if (civilization != null) {
                return civilization;
            } else {
                this.civilizationUUID = null;
                this.setChanged();
            }
        }

        return null;
    }

    @Nullable
    public Location getLocation() {
        if (this.locationUUID != null) {
            Location location = CivilizationRepositories.getLocationRepository()
                    .getById(this.locationUUID);
            if (location != null) {
                return location;
            } else {
                this.locationUUID = null;
                this.setChanged();
            }
        }

        return null;
    }

    public boolean canBeNamedBy(Entity entity) {
        Civilization civilization = this.getCivilization();
        if (civilization != null) {
            return CivilizationRepositories.getCivilizationRepository()
                    .getCitizens(civilization)
                    .contains(entity.getUUID());
        }
        return false;
    }

    @Nullable
    public Component getCustomName() {
        return this.getName();
    }

    public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns() {
        if (this.patterns == null) {
            this.patterns = BannerBlockEntity.createPatterns(this.dyeColor, this.itemPatterns);
        }

        return this.patterns;
    }

    @Override
    @NotNull
    public Component getName() {
        if (this.name == null && this.civilizationUUID != null) {
            this.name = switch (this.getBannerType()) {
                case CAPITAL -> Optional.ofNullable(this.getCivilization())
                        .map(Civilization::getName)
                        .orElse(null);
                case CITY -> Optional.ofNullable(this.getLocation())
                        .map(Location::getName)
                        .orElse(null);
                default -> null;
            };
        }
        return this.name != null ? this.name : this.getBlockState().getBlock().getName();
    }

    public void checkRefresh(boolean update) {
        Civilization civilization = this.getCivilization();
        if (civilization != null) {
            this.name = civilization.getName();
            this.fromItem(civilization.getBanner());
        } else {
            this.name = null;
            this.fromItem(ItemStack.EMPTY);
        }
        if (this.getBannerType() == CivilizationBannerType.CITY) {
            Location location = this.getLocation();
            if (location != null) {
                this.name = location.getName();
            }
        }
        if (update && this.level != null) {
            this.setChanged();
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.hasUUID("CivilizationUUID")) {
            this.civilizationUUID = pTag.getUUID("CivilizationUUID");
        }
        if (pTag.hasUUID("LocationUUID")) {
            this.locationUUID = pTag.getUUID("LocationUUID");
        }
        this.checkRefresh(false);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (this.civilizationUUID != null) {
            pTag.putUUID("CivilizationUUID", this.civilizationUUID);
        }
        if (this.locationUUID != null) {
            pTag.putUUID("LocationUUID", this.locationUUID);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        Civilization civilization = this.getCivilization();
        if (civilization != null) {
            tag.put("Civilization", civilization.toTag());
        }
        Location location = this.getLocation();
        if (location != null) {
            tag.put("Location", location.toNBT());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag.contains("Civilization")) {
            Civilization civilization = Civilization.fromTag(tag.getCompound("Civilization"));
            this.fromItem(civilization.getBanner());
            this.name = civilization.getName();
            if (tag.contains("Location") && this.getBannerType() == CivilizationBannerType.CITY) {
                Location location = Location.fromNBT(tag.getCompound("Location"));
                this.name = location.getName();
            }
        } else {
            this.fromItem(ItemStack.EMPTY);
            this.name = null;
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.handleUpdateTag(tag);
        }
    }

    public InteractionResult handleCityBanner(Player pPlayer, ItemStack itemInHand) {
        boolean isClient = Objects.requireNonNull(this.getLevel()).isClientSide();
        if (itemInHand.is(Items.NAME_TAG) && itemInHand.hasCustomHoverName()) {
            ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();
            Civilization playerCivilization = civilizationRepository.getCivilizationByCitizen(pPlayer);
            Civilization bannerCivilization = this.getCivilization();

            if (playerCivilization == bannerCivilization) {
                this.renameLocation(bannerCivilization, itemInHand.getHoverName());
                return InteractionResult.sidedSuccess(isClient);
            }
        }


        return InteractionResult.FAIL;
    }

    public InteractionResult handleCapitalBanner(Player pPlayer, ItemStack itemInHand) {
        boolean isClient = Objects.requireNonNull(this.getLevel()).isClientSide();
        ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();
        Civilization playerCivilization = civilizationRepository.getCivilizationByCitizen(pPlayer);
        Civilization bannerCivilization = this.getCivilization();
        if (bannerCivilization != null) {
            if (itemInHand.is(Items.PAPER)) {
                boolean canJoin = playerCivilization == null;
                if (playerCivilization != null && playerCivilization != bannerCivilization) {
                    canJoin = civilizationRepository.leaveCivilization(playerCivilization, pPlayer);
                    if (canJoin && !isClient) {
                        pPlayer.sendSystemMessage(MCCivilizationsText.CIVILIZATION_LEFT);
                    }
                }
                boolean joined = false;
                if (canJoin) {
                    joined = civilizationRepository.joinCivilization(bannerCivilization, pPlayer);
                }
                if (joined && !isClient) {
                    pPlayer.sendSystemMessage(MCCivilizationsText.CIVILIZATION_JOINED);
                }
                return joined ? InteractionResult.sidedSuccess(isClient) : InteractionResult.FAIL;
            } else if (itemInHand.canPerformAction(ToolActions.SWORD_SWEEP)) {
                ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();
                if (playerCivilization != null) {
                    landClaimRepository.transferClaims(bannerCivilization, playerCivilization);
                    civilizationRepository.removeCivilization(bannerCivilization);
                    this.civilizationUUID = null;
                    this.setChanged();
                    if (!isClient) {
                        pPlayer.sendSystemMessage(MCCivilizationsText.CIVILIZATION_CONQUERED);
                    }
                    return InteractionResult.sidedSuccess(isClient);
                } else {
                    if (civilizationRepository.joinCivilization(bannerCivilization, pPlayer)) {
                        if (!isClient) {
                            pPlayer.sendSystemMessage(MCCivilizationsText.CIVILIZATION_JOINED);
                        }
                        return InteractionResult.sidedSuccess(isClient);
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
            } else if (itemInHand.is(Items.NAME_TAG) && itemInHand.hasCustomHoverName()) {
                if (bannerCivilization == playerCivilization) {
                    this.renameCivilization(bannerCivilization, itemInHand.getHoverName());
                    return InteractionResult.sidedSuccess(isClient);
                }
            }
        }


        return InteractionResult.PASS;
    }

    private void renameLocation(Civilization civilization, Component hoverName) {
        Location location = this.getLocation();
        if (location != null) {
            location.setName(hoverName);
            CivilizationRepositories.getLocationRepository()
                    .upsertLocation(civilization, location);
            this.name = null;
        }
    }

    private void renameCivilization(Civilization civilization, Component name) {
        civilization.setName(name);
        CivilizationRepositories.getCivilizationRepository()
                .upsertCivilization(civilization);
        this.renameLocation(civilization, name);
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public void rename(Component component) {
        Civilization civilization = this.getCivilization();
        if (component != null && civilization != null) {
            CivilizationBannerType civilizationBannerType = this.getBannerType();
            if (civilizationBannerType == CivilizationBannerType.CAPITAL) {
                this.renameCivilization(civilization, component);
            } else if (civilizationBannerType == CivilizationBannerType.CITY) {
                this.renameLocation(civilization, component);
            }
        }
    }

    private CivilizationBannerType getBannerType() {
        if (this.getBlockState().getBlock() instanceof AbstractCivilizationBannerBlock bannerType) {
            return bannerType.getBannerType();
        }
        return CivilizationBannerType.DECOR;
    }

    public void removeLocation() {
        Location location = this.getLocation();
        Civilization civilization = this.getCivilization();

        if (civilization != null && location != null) {
            CivilizationRepositories.getLocationRepository()
                    .removeLocation(civilization, location);
        }
    }
}

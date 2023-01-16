package xyz.brassgoggledcoders.mccivilizations.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsBlocks;

import java.util.UUID;

public class CivilizationBannerBlockEntity extends BannerBlockEntity {
    private UUID civilizationUUID;

    public CivilizationBannerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    public CivilizationBannerBlockEntity(BlockEntityType<?> ignored, BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState);
    }

    public void setCivilizationUUID(UUID uuid) {
        this.civilizationUUID = uuid;
        Civilization civilization = this.getCivilization();
        if (civilization == null) {
            this.civilizationUUID = null;
        } else {
            this.fromItem(civilization.getBanner());
            this.setCustomName(civilization.getName());
        }
    }

    private Civilization getCivilization() {
        if (this.civilizationUUID != null) {
            return CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationById(this.civilizationUUID);
        }

        return null;
    }

    @Override
    @NotNull
    public Component getName() {
        Civilization civilization = this.getCivilization();
        return civilization != null ? civilization.getName() : super.getName();
    }

    @Override
    @NotNull
    public BlockEntityType<?> getType() {
        return MCCivilizationsBlocks.CIVILIZATION_BANNER_BLOCK_ENTITY.get();
    }

    public void checkRefresh() {

    }
}

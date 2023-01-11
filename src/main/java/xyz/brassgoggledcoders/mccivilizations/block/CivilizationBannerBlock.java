package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationServices;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

public class CivilizationBannerBlock extends BannerBlock {
    public CivilizationBannerBlock(DyeColor pColor, Properties pProperties) {
        super(pColor, pProperties);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer instanceof Player player && pLevel.getBlockEntity(pPos) instanceof BannerBlockEntity bannerBlockEntity) {
            ICivilizations civilizations = CivilizationServices.getCivilizationService(pLevel);
            Civilization playerCivilization = civilizations.getCivilizationByCitizen(player);
            if (playerCivilization != null) {
                bannerBlockEntity.fromItem(playerCivilization.getBanner());
            } else {

                Component name = MCCivilizationsText.NO_NAME_CIVILIZATION;
                if (pStack.hasCustomHoverName()) {
                    name = pStack.getHoverName();
                }

                Civilization newCivilization = new Civilization(
                        UUID.randomUUID(),
                        name,
                        pStack
                );
                civilizations.createCivilization(newCivilization);
                civilizations.joinCivilization(newCivilization, player);
            }
        }
    }
}

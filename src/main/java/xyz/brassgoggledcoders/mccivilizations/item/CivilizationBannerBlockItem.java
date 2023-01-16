package xyz.brassgoggledcoders.mccivilizations.item;

import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import xyz.brassgoggledcoders.mccivilizations.block.StandingCivilizationBannerBlock;

public class CivilizationBannerBlockItem extends StandingAndWallBlockItem {
    public CivilizationBannerBlockItem(StandingCivilizationBannerBlock pStandingBlock, Block pWallBlock, Properties pProperties) {
        super(pStandingBlock, pWallBlock, pProperties);
    }
}

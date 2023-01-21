package xyz.brassgoggledcoders.mccivilizations.compat.naming;

import com.minerarcana.naming.api.blockentity.NameableBlockEntityWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.blockentity.CivilizationBannerBlockEntity;

public class NameableCivilizationBanner extends NameableBlockEntityWrapper<CivilizationBannerBlockEntity> {
    public NameableCivilizationBanner(CivilizationBannerBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public void setCustomName(Component component) {
        this.getBlockEntity().renameCivilization(component);
    }

    @Override
    public Component getOriginalName() {
        return this.getName();
    }

    @Override
    public boolean canName(Entity entity) {
        return this.getBlockEntity().canBeNamedBy(entity);
    }

    public static NameableCivilizationBanner forBlock(BlockEntity blockEntity) {
        if (blockEntity instanceof CivilizationBannerBlockEntity bannerBlockEntity) {
            return new NameableCivilizationBanner(bannerBlockEntity);
        }
        return null;
    }
}

package xyz.brassgoggledcoders.mccivilizations.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.service.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.block.CivilizationBannerType;
import xyz.brassgoggledcoders.mccivilizations.block.StandingCivilizationBannerBlock;
import xyz.brassgoggledcoders.mccivilizations.render.CivilizationBannerItemStackRender;

import java.util.List;
import java.util.function.Consumer;

public class CivilizationBannerBlockItem extends StandingAndWallBlockItem {
    private static final CompoundTag EMPTY_BANNER = Util.make(() -> {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("Base", DyeColor.WHITE.getId());
        return compoundTag;
    });

    private final CivilizationBannerType type;

    public CivilizationBannerBlockItem(StandingCivilizationBannerBlock pStandingBlock, Block pWallBlock, Properties pProperties) {
        super(pStandingBlock, pWallBlock, pProperties);
        this.type = pStandingBlock.getBannerType();
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull UseOnContext pContext) {
        if (pContext.getPlayer() != null) {
            Civilization userCivilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(pContext.getPlayer());
            if ((userCivilization == null) == (this.getType() == CivilizationBannerType.CAPITAL)) {
                return super.useOn(pContext);
            }
        }

        return InteractionResult.FAIL;
    }

    public CivilizationBannerType getType() {
        return type;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CivilizationBannerItemStackRender.INSTANCE;
            }
        });
    }

    public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns(@Nullable Player player, ItemStack itemStack) {
        CompoundTag bannerTag = null;
        if (this.getType() == CivilizationBannerType.CAPITAL) {
            bannerTag = BlockItem.getBlockEntityData(itemStack);
        } else if (player != null) {
            Civilization civilization = CivilizationRepositories.getCivilizationRepository()
                    .getCivilizationByCitizen(player);
            if (civilization != null) {
                bannerTag = BlockItem.getBlockEntityData(civilization.getBanner());
            }
        }
        if (bannerTag == null) {
            bannerTag = EMPTY_BANNER;
        }
        DyeColor dyeColor = DyeColor.WHITE;
        if (bannerTag.contains("Base", Tag.TAG_INT)) {
            dyeColor = DyeColor.byId(bannerTag.getInt("Base"));
        }
        return BannerBlockEntity.createPatterns(dyeColor, bannerTag.getList("Patterns", Tag.TAG_COMPOUND));
    }
}

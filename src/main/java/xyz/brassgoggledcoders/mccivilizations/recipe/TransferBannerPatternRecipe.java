package xyz.brassgoggledcoders.mccivilizations.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsBlocks;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsRecipes;

public record TransferBannerPatternRecipe(
        ResourceLocation id,
        Ingredient otherItem,
        ItemStack result
) implements CraftingRecipe {
    @Override
    public boolean matches(@NotNull CraftingContainer pInv, @NotNull Level pLevel) {
        boolean foundOther = false;
        boolean foundBanner = false;

        for (int i = 0; i < pInv.getContainerSize(); ++i) {
            ItemStack containerItemStack = pInv.getItem(i);
            if (!containerItemStack.isEmpty()) {
                if (containerItemStack.getItem() instanceof BannerItem) {
                    if (foundBanner) {
                        return false;
                    }

                    foundBanner = true;
                } else if (this.otherItem.test(containerItemStack)) {
                    if (foundOther) {
                        return false;
                    }

                    foundOther = true;
                } else {
                    return false;
                }
            }
        }

        return foundOther && foundBanner;
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull CraftingContainer pContainer) {
        ItemStack assembledResult = this.result().copy();
        CompoundTag bannerPattern = null;

        for (int i = 0; i < pContainer.getContainerSize(); ++i) {
            ItemStack containerItemStack = pContainer.getItem(i);
            if (!containerItemStack.isEmpty()) {
                if (containerItemStack.getItem() instanceof BannerItem bannerItem) {
                    bannerPattern = BlockItem.getBlockEntityData(containerItemStack);
                    if (bannerPattern != null) {
                        bannerPattern = bannerPattern.copy();
                        bannerPattern.putInt("Base", bannerItem.getColor().getId());
                    }
                }
            }
        }

        if (bannerPattern != null) {
            BlockItem.setBlockEntityData(
                    assembledResult,
                    MCCivilizationsBlocks.CIVILIZATION_BANNER_BLOCK_ENTITY.get(),
                    bannerPattern
            );
        }
        return assembledResult;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pHeight * pHeight >= 2;
    }

    @Override
    @NotNull
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public ResourceLocation getId() {
        return this.id();
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return MCCivilizationsRecipes.TRANSFER_BANNER_PATTERN.get();
    }
}

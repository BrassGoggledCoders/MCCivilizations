package xyz.brassgoggledcoders.mccivilizations.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class TransferBannerPatternRecipeSerializer implements RecipeSerializer<TransferBannerPatternRecipe> {
    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public TransferBannerPatternRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
        return new TransferBannerPatternRecipe(
                pRecipeId,
                Ingredient.fromJson(pSerializedRecipe.get("ingredient")),
                CraftingHelper.getItemStack(
                        GsonHelper.getAsJsonObject(pSerializedRecipe, "result"),
                        true
                )
        );
    }

    @Override
    @Nullable
    @ParametersAreNonnullByDefault
    public TransferBannerPatternRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        return new TransferBannerPatternRecipe(
                pRecipeId,
                Ingredient.fromNetwork(pBuffer),
                pBuffer.readItem()
        );
    }

    @Override
    @ParametersAreNonnullByDefault
    public void toNetwork(FriendlyByteBuf pBuffer, TransferBannerPatternRecipe pRecipe) {
        pRecipe.otherItem().toNetwork(pBuffer);
        pBuffer.writeItem(pRecipe.result());
    }
}

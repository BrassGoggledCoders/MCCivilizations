package xyz.brassgoggledcoders.mccivilizations.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsRecipes;

import java.util.Objects;
import java.util.function.Consumer;

public class TransferBannerPatternRecipeBuilder {
    private final ItemStack result;
    private Ingredient ingredient;

    public TransferBannerPatternRecipeBuilder(ItemStack result) {
        this.result = result;
    }

    public TransferBannerPatternRecipeBuilder withIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(result.getItem());
        if (itemId != null) {
            save(finishedRecipeConsumer, itemId);
        } else {
            throw new IllegalArgumentException("Failed to get Id for Result");
        }
    }

    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
        Validate.notNull(ingredient, "ingredient cannot be null");
        Validate.isTrue(!result.isEmpty(), "result cannot be empty");
        finishedRecipeConsumer.accept(new TransferBannerPatternFinishedRecipe(
                recipeId,
                ingredient,
                result
        ));
    }

    public static TransferBannerPatternRecipeBuilder of(ItemLike itemLike) {
        return new TransferBannerPatternRecipeBuilder(new ItemStack(itemLike));
    }

    public record TransferBannerPatternFinishedRecipe(
            ResourceLocation recipeId,
            Ingredient ingredient,
            ItemStack result
    ) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(@NotNull JsonObject pJson) {
            pJson.add("ingredient", ingredient.toJson());
            pJson.add("result", writeItemStack(result));
        }

        @Override
        @NotNull
        public ResourceLocation getId() {
            return recipeId;
        }

        @Override
        @NotNull
        public RecipeSerializer<?> getType() {
            return MCCivilizationsRecipes.TRANSFER_BANNER_PATTERN.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }

        public static JsonElement writeItemStack(ItemStack result) {
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(result.getItem())).toString());
            if (result.getCount() > 1) {
                resultObject.addProperty("count", result.getCount());
            }
            if (result.getTag() != null) {
                resultObject.addProperty("nbt", result.getTag().toString());
            }
            return resultObject;
        }
    }
}

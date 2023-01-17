package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

public enum CivilizationBannerType {
    CAPITAL("Capital", Ingredient.of(Tags.Items.INGOTS_GOLD)),
    CITY("City", Ingredient.of(Tags.Items.INGOTS_IRON)),
    DECOR("Decorative", Ingredient.of(Tags.Items.INGOTS_COPPER));

    private final String lang;
    private final Ingredient ingredient;

    CivilizationBannerType(String lang, Ingredient ingredient) {
        this.lang = lang;
        this.ingredient = ingredient;
    }

    public String getLang() {
        return this.lang;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }
}

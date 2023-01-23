package xyz.brassgoggledcoders.mccivilizations.block;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsLocationTypes;

import java.util.function.Supplier;

public enum CivilizationBannerType {
    CAPITAL("Capital", Ingredient.of(Tags.Items.INGOTS_GOLD), MCCivilizationsLocationTypes.CAPITAL),
    CITY("City", Ingredient.of(Tags.Items.INGOTS_IRON), MCCivilizationsLocationTypes.CITY),
    DECOR("Decorative", Ingredient.of(Tags.Items.INGOTS_COPPER), null);

    private final String lang;
    private final Ingredient ingredient;
    private final Supplier<LocationType> locationType;

    CivilizationBannerType(String lang, Ingredient ingredient, Supplier<LocationType> locationType) {
        this.lang = lang;
        this.ingredient = ingredient;
        this.locationType = locationType;
    }

    public String getLang() {
        return this.lang;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public LocationType getLocationType() {
        return locationType.get();
    }

    public boolean canHaveMoreOf(@Nullable Civilization civilization) {
        if (civilization == null && this == CAPITAL) {
            return true;
        } else if (civilization != null) {
            LocationType currentType = this.getLocationType();
            return CivilizationRepositories.getLocationRepository()
                    .canHaveMore(civilization, currentType);
        }

        return false;
    }
}

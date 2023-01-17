package xyz.brassgoggledcoders.mccivilizations.content;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.recipe.TransferBannerPatternRecipeSerializer;

public class MCCivilizationsRecipes {

    public static final RegistryEntry<TransferBannerPatternRecipeSerializer> TRANSFER_BANNER_PATTERN =
            MCCivilizations.getRegistrate()
                    .object("transfer_banner_pattern")
                    .simple(Registry.RECIPE_SERIALIZER_REGISTRY, TransferBannerPatternRecipeSerializer::new);
    public static void setup() {

    }
}

package xyz.brassgoggledcoders.mccivilizations.content;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;

import java.util.Objects;

public class MCCivilizationsEntityTags {

    public static final TagKey<EntityType<?>> IGNORES_CLAIMS = create(MCCivilizations.rl("ignores_claims"));

    private static TagKey<EntityType<?>> create(ResourceLocation resourceLocation) {
        return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.tags())
                .createTagKey(resourceLocation);
    }

    public static void generate(RegistrateTagsProvider<EntityType<?>> tagsProvider) {
        tagsProvider.tag(IGNORES_CLAIMS)
                .addTag(Tags.EntityTypes.BOSSES);
    }
}

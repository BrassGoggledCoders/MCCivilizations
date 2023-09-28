package xyz.brassgoggledcoders.mccivilizations.content;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.resource.Resource;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class MCCivilizationsDataPackRegistries {
    public static final ResourceKey<Registry<Resource>> RESOURCE_KEY = MCCivilizations.getRegistrate()
            .makeRegistry("resource", () -> new RegistryBuilder<Resource>()
                    .dataPackRegistry(Resource.CODEC, Resource.CODEC)
                    .disableSaving()
            );

    public static void setup() {

    }
}

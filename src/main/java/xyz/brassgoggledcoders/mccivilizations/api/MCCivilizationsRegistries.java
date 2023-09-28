package xyz.brassgoggledcoders.mccivilizations.api;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationType;
import xyz.brassgoggledcoders.mccivilizations.api.resource.Resource;

import java.util.Optional;
import java.util.function.Supplier;

public class MCCivilizationsRegistries {
    private final static LocationType MISSING = new LocationType(Integer.MAX_VALUE);
    public static final ResourceKey<Registry<LocationType>> LOCATION_TYPE = ResourceKey.createRegistryKey(new ResourceLocation(
            "mccivilizations",
            "location_type"
    ));

    public static final Supplier<IForgeRegistry<LocationType>> LOCATION_TYPE_REGISTRY = Suppliers.memoize(() ->
            RegistryManager.ACTIVE.getRegistry(LOCATION_TYPE)
    );

    public static final ResourceKey<Registry<Resource>> RESOURCE = ResourceKey.createRegistryKey(new ResourceLocation(
            "mccivilizations",
            "resource"
    ));

    public static final Supplier<IForgeRegistry<Resource>> RESOURCE_REGISTRY = Suppliers.memoize(() ->
            RegistryManager.ACTIVE.getRegistry(RESOURCE)
    );

    public static String getLocationTypeKey(LocationType locationType) {
        return Optional.ofNullable(LOCATION_TYPE_REGISTRY.get().getKey(locationType))
                .map(ResourceLocation::toString)
                .orElse("mccivilizations:unknown");
    }

    public static LocationType getLocationType(String locationType) {
        if (locationType == null || locationType.isBlank()) {
            locationType = "mccivilizations:unknown";
        }
        return Optional.ofNullable(LOCATION_TYPE_REGISTRY.get()
                        .getValue(new ResourceLocation(locationType))
                )
                .orElse(MISSING);
    }
}

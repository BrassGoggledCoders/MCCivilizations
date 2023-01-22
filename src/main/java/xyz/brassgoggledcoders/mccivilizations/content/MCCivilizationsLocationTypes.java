package xyz.brassgoggledcoders.mccivilizations.content;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationType;

@SuppressWarnings("UnstableApiUsage")
public class MCCivilizationsLocationTypes {
    private static final ResourceKey<Registry<LocationType>> RESOURCE_KEY = MCCivilizations.getRegistrate()
            .makeRegistry("location_type", RegistryBuilder::new);
    public static final RegistryEntry<LocationType> CAPITAL = MCCivilizations.getRegistrate()
            .object("capital")
            .simple(RESOURCE_KEY, () -> new LocationType(1));

    public static final RegistryEntry<LocationType> CITY = MCCivilizations.getRegistrate()
            .object("city")
            .simple(RESOURCE_KEY, () -> new LocationType(10));

    public static final RegistryEntry<LocationType> UNKNOWN = MCCivilizations.getRegistrate()
            .object("unknown")
            .simple(RESOURCE_KEY, () -> new LocationType(Integer.MAX_VALUE));

    public static void setup() {
    }
}

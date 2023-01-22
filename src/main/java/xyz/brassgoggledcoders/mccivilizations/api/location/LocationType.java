package xyz.brassgoggledcoders.mccivilizations.api.location;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.MCCivilizationsRegistries;

public class LocationType {
    private final int maxPerCivilization;
    @Nullable
    private String descriptionId;

    public LocationType(int maxPerCivilization) {
        this.maxPerCivilization = maxPerCivilization;
    }

    public MutableComponent getName() {
        return Component.translatable(this.getDescriptionId());
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId(
                    "location_type",
                    MCCivilizationsRegistries.LOCATION_TYPE.location()
            );
        }

        return this.descriptionId;
    }

    public int getMaxPerCivilization() {
        return maxPerCivilization;
    }
}

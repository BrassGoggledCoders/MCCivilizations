package xyz.brassgoggledcoders.mccivilizations.location;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.ILocationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationType;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.Collection;
import java.util.UUID;

public class LocationRepository extends Repository implements ILocationRepository {
    private final Table<UUID, UUID, Location> locations;

    private final ICivilizationRepository civilizationRepository;
    private final boolean sync;

    public LocationRepository(ICivilizationRepository civilizationRepository, boolean sync) {
        super("location");
        this.sync = sync;
        this.civilizationRepository = civilizationRepository;
        this.locations = HashBasedTable.create();
    }

    @Override
    public boolean canHaveMore(Civilization civilization, LocationType locationType) {
        long amountOfType = this.getLocations(civilization)
                .stream()
                .filter(location -> location.getLocationType() == locationType)
                .count();

        return amountOfType < locationType.getMaxPerCivilization();
    }

    @Override
    public void addLocation(Civilization civilization, Location location) {
        if (civilizationRepository.civilizationExists(civilization.getId())) {
            if (locations.column(location.getId()).values().isEmpty()) {
                locations.put(civilization.getId(), location.getId(), location);
                this.addDirtyId(civilization.getId());
            }
        }
    }

    @Override
    public void removeLocation(Civilization civilization, Location location) {
        if (locations.contains(location.getId(), location.getId())) {
            locations.remove(civilization.getId(), location.getId());
            this.addDirtyId(civilization.getId());
        }
    }

    @Override
    public Collection<Location> getLocationsOf(Civilization civilization, LocationType locationType) {
        return this.getLocations(civilization)
                .stream()
                .filter(location -> location.getLocationType() == locationType)
                .toList();
    }

    @Override
    public Collection<Location> getLocations(Civilization civilization) {
        return locations.row(civilization.getId()).values();
    }

    @Override
    @Nullable
    public Location getById(UUID uuid) {
        return locations.column(uuid).values()
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Nullable
    public CompoundTag getSerializedValue(UUID id) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("CivilizationId", id);
        Collection<Location> locationsToSerialize = locations.row(id).values();
        ListTag locationsTag = new ListTag();
        for (Location location : locationsToSerialize) {
            locationsTag.add(location.toNBT());
        }
        tag.put("Locations", locationsTag);
        return tag;
    }

    @Override
    public void deserializeAndInsertValue(@NotNull CompoundTag tag) {
        UUID civilizationId = tag.getUUID("CivilizationId");
        ListTag locationsTag = tag.getList("Locations", Tag.TAG_COMPOUND);
        for (int i = 0; i < locationsTag.size(); i++) {
            Location location = Location.fromNBT(locationsTag.getCompound(i));
            this.locations.put(
                    civilizationId,
                    location.getId(),
                    location
            );
        }
    }
}

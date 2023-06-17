package xyz.brassgoggledcoders.mccivilizations.location;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.ILocationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.Location;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationChangedEvent;
import xyz.brassgoggledcoders.mccivilizations.api.location.LocationType;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;
import xyz.brassgoggledcoders.mccivilizations.network.LocationUpdatePacket;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;
import xyz.brassgoggledcoders.mccivilizations.repository.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
    public void upsertLocation(Civilization civilization, Location location) {
        if (civilizationRepository.civilizationExists(civilization.getId())) {
            Map<UUID, Location> locationById = locations.column(location.getId());
            if (locationById.isEmpty() || (locationById.size() == 1 && locationById.containsKey(civilization.getId()))) {
                locations.put(civilization.getId(), location.getId(), location);
                this.addDirtyId(civilization.getId());
                MinecraftForge.EVENT_BUS.post(new LocationChangedEvent(civilization, location, ChangeType.ADD));
                if (this.sync) {
                    NetworkHandler.getInstance()
                            .sendPacketToAll(new LocationUpdatePacket(
                                    civilization.getId(),
                                    Collections.singleton(location),
                                    ChangeType.ADD,
                                    15
                            ));
                }
            }
        }
    }

    @Override
    public void removeLocation(Civilization civilization, Location location) {
        if (locations.contains(civilization.getId(), location.getId())) {
            locations.remove(civilization.getId(), location.getId());
            this.addDirtyId(civilization.getId());
            MinecraftForge.EVENT_BUS.post(new LocationChangedEvent(civilization, location, ChangeType.REMOVE));
            if (this.sync) {
                NetworkHandler.getInstance()
                        .sendPacketToAll(new LocationUpdatePacket(
                                civilization.getId(),
                                Collections.singleton(location),
                                ChangeType.REMOVE,
                                15
                        ));
            }
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
    public Collection<Location> getAllLocations() {
        return locations.values();
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
    public Collection<UUID> getIds() {
        return this.locations.rowKeySet();
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

    @Override
    public void onPlayerJoin(ServerPlayer serverPlayer) {
        Civilization playerCivilization = this.civilizationRepository.getCivilizationByCitizen(serverPlayer);

        if (playerCivilization != null) {
            NetworkHandler.getInstance()
                    .sendPacket(serverPlayer, new LocationUpdatePacket(
                            playerCivilization.getId(),
                            this.getLocations(playerCivilization),
                            ChangeType.ADD,
                            5
                    ));
        }
    }
}

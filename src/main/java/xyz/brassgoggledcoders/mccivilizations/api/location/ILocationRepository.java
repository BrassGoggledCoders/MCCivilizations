package xyz.brassgoggledcoders.mccivilizations.api.location;

import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;

import java.util.Collection;
import java.util.UUID;

public interface ILocationRepository {

    boolean canHaveMore(Civilization civilization, LocationType locationType);

    void upsertLocation(Civilization civilization, Location location);

    void removeLocation(Civilization civilization, Location location);

    Collection<Location> getLocationsOf(Civilization civilization, LocationType locationType);

    Collection<Location> getLocations(Civilization civilization);

    Collection<Location> getAllLocations();

    Location getById(UUID uuid);
}

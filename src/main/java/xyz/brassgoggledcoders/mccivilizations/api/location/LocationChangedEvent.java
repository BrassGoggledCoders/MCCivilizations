package xyz.brassgoggledcoders.mccivilizations.api.location;

import net.minecraftforge.eventbus.api.Event;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;

public class LocationChangedEvent extends Event {
    private final Civilization civilization;
    private final Location location;
    private final ChangeType changeType;

    public LocationChangedEvent(Civilization civilization, Location location, ChangeType changeType) {
        this.civilization = civilization;
        this.location = location;
        this.changeType = changeType;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Civilization getCivilization() {
        return civilization;
    }

    public Location getLocation() {
        return location;
    }
}

package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraftforge.eventbus.api.Event;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ChangeType;

public class CivilizationChangedEvent extends Event {
    private final Civilization civilization;
    private final ChangeType changeType;

    public CivilizationChangedEvent(Civilization civilization, ChangeType changeType) {
        this.civilization = civilization;
        this.changeType = changeType;
    }

    public Civilization getCivilization() {
        return civilization;
    }

    public ChangeType getChangeType() {
        return changeType;
    }
}

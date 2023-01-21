package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("unused")
public interface ICivilizationRepository {
    @Nullable
    Civilization getCivilizationByCitizen(@NotNull Entity entity);

    @Nullable
    Civilization getCivilizationById(UUID id);

    default boolean civilizationExists(UUID id) {
        return this.getCivilizationById(id) != null;
    }

    Collection<UUID> getCitizens(Civilization civilization);

    void upsertCivilization(Civilization civilization);

    default boolean joinCivilization(Civilization civilization, Entity player) {
        return this.joinCivilization(civilization, player.getUUID());
    }

    boolean joinCivilization(Civilization civilization, UUID player);

    default boolean leaveCivilization(Civilization civilization, Entity citizen) {
        return this.leaveCivilization(civilization, citizen.getUUID());
    }

    boolean leaveCivilization(Civilization civilization, UUID citizen);

    void removeCivilization(Civilization civilization);

    Collection<Civilization> getAllCivilizations();
}

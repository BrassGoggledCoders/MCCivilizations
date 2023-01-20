package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface ICivilizationRepository {
    @Nullable
    Civilization getCivilizationByCitizen(@NotNull Entity entity);

    @Nullable
    Civilization getCivilizationById(UUID id);

    Collection<UUID> getCitizens(Civilization civilization);

    void upsertCivilization(Civilization civilization);

    boolean joinCivilization(Civilization civilization, Entity player);

    boolean leaveCivilization(Civilization civilization, Entity citizen);

    void removeCivilization(Civilization civilization);

    Collection<Civilization> getAllCivilizations();
}

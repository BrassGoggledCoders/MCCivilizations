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

    void joinCivilization(Civilization civilization, Entity player);

    void removeCivilization(Civilization civilization);
}

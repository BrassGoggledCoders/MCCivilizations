package xyz.brassgoggledcoders.mccivilizations.api.civilization;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ICivilizations {
    @Nullable
    Civilization getCivilizationByCitizen(@NotNull Entity entity);

    @Nullable
    Civilization getCivilizationById(UUID id);

    void upsertCivilization(Civilization civilization);

    void joinCivilization(Civilization newCivilization, Entity player);
}

package xyz.brassgoggledcoders.mccivilizations.api.service;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;

public interface ICivilizationRepositoryProvider {
    ICivilizationRepository getCivilizationRepository();

    ILandClaimRepository getLandClaimRepository(@Nullable Level level);
}

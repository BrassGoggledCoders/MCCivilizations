package xyz.brassgoggledcoders.mccivilizations.api.service;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.IClaimedLand;

public interface ICivilizationServiceProvider {
    ICivilizationRepository getCivilizations(@Nullable Level level);

    IClaimedLand getClaimedLand(@Nullable Level level);
}

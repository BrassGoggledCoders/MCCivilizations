package xyz.brassgoggledcoders.mccivilizations.service;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.IClaimedLand;
import xyz.brassgoggledcoders.mccivilizations.api.service.ICivilizationServiceProvider;
import xyz.brassgoggledcoders.mccivilizations.civilization.Civilizations;
import xyz.brassgoggledcoders.mccivilizations.claim.ClaimedLandSavedData;
import xyz.brassgoggledcoders.mccivilizations.claim.ClientClaimedLand;

public class CivilizationServiceProvider implements ICivilizationServiceProvider {
    private final Civilizations clientCivilizations = new Civilizations();
    private ICivilizationRepository serverCivilizations = new Civilizations();
    private final IClaimedLand clientClaims = new ClientClaimedLand(clientCivilizations);

    @Override
    public ICivilizationRepository getCivilizations(@Nullable Level level) {
        if (level instanceof ServerLevel) {
            return serverCivilizations;
        } else {
            return clientCivilizations;
        }
    }

    @Override
    public IClaimedLand getClaimedLand(@Nullable Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage()
                    .computeIfAbsent(
                            nbt -> new ClaimedLandSavedData(serverCivilizations, nbt),
                            () -> new ClaimedLandSavedData(serverCivilizations),
                            "claimed_land"
                    );
        } else {
            return clientClaims;
        }
    }

    public void setServerCivilizations(ICivilizationRepository civilizations) {
        this.serverCivilizations = civilizations;
    }
}

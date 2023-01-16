package xyz.brassgoggledcoders.mccivilizations.service;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.service.ICivilizationRepositoryProvider;
import xyz.brassgoggledcoders.mccivilizations.civilization.Civilizations;
import xyz.brassgoggledcoders.mccivilizations.claim.LandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.claim.ClientLandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.repository.RepositoryManager;

public class CivilizationRepositoryProvider implements ICivilizationRepositoryProvider {
    private final Civilizations clientCivilizations = new Civilizations();
    private final ILandClaimRepository clientClaims = new ClientLandClaimRepository(clientCivilizations);

    @Override
    public ICivilizationRepository getCivilizationRepository() {
        if (RepositoryManager.INSTANCE != null) {
            return RepositoryManager.INSTANCE.getCivilizationRepository();
        } else {
            return clientCivilizations;
        }
    }

    @Override
    public ILandClaimRepository getLandClaimRepository(@Nullable Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage()
                    .computeIfAbsent(
                            nbt -> new LandClaimRepository(serverCivilizations, nbt),
                            () -> new LandClaimRepository(serverCivilizations),
                            "claimed_land"
                    );
        } else {
            return clientClaims;
        }
    }
}

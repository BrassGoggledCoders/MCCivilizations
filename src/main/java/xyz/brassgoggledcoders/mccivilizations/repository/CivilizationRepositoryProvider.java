package xyz.brassgoggledcoders.mccivilizations.repository;

import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ICivilizationRepositoryProvider;
import xyz.brassgoggledcoders.mccivilizations.civilization.CivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.claim.LandClaimRepository;

public class CivilizationRepositoryProvider implements ICivilizationRepositoryProvider {
    private final ICivilizationRepository clientCivilizations = new CivilizationRepository();
    private final ILandClaimRepository clientClaims = new LandClaimRepository(clientCivilizations);

    @Override
    public ICivilizationRepository getCivilizationRepository() {
        if (RepositoryManager.INSTANCE != null) {
            return RepositoryManager.INSTANCE.getCivilizationRepository();
        } else {
            return clientCivilizations;
        }
    }

    @Override
    public ILandClaimRepository getLandClaimRepository() {
        if (RepositoryManager.INSTANCE != null) {
            return RepositoryManager.INSTANCE.getLandClaimRepository();
        } else {
            return clientClaims;
        }
    }
}

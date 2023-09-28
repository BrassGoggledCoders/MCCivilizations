package xyz.brassgoggledcoders.mccivilizations.repository;

import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.ILocationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.ICivilizationRepositoryProvider;
import xyz.brassgoggledcoders.mccivilizations.api.resource.IResourceRepository;
import xyz.brassgoggledcoders.mccivilizations.civilization.CivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.claim.LandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.location.LocationRepository;

public class CivilizationRepositoryProvider implements ICivilizationRepositoryProvider {
    private final ICivilizationRepository clientCivilizations = new CivilizationRepository(false);
    private final ILandClaimRepository clientClaims = new LandClaimRepository(clientCivilizations, false);

    private final ILocationRepository clientLocations = new LocationRepository(clientCivilizations, false);

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

    @Override
    public ILocationRepository getLocationRepository() {
        if (RepositoryManager.INSTANCE != null) {
            return RepositoryManager.INSTANCE.getLocationRepository();
        } else {
            return clientLocations;
        }
    }

    @Override
    public IResourceRepository getResourceRepository() {
        return null;
    }
}

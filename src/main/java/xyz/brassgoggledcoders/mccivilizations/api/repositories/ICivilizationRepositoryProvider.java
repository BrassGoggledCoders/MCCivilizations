package xyz.brassgoggledcoders.mccivilizations.api.repositories;

import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.ILocationRepository;

public interface ICivilizationRepositoryProvider {
    ICivilizationRepository getCivilizationRepository();

    ILandClaimRepository getLandClaimRepository();

    ILocationRepository getLocationRepository();
}

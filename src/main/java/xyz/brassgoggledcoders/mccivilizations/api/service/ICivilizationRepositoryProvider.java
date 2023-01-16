package xyz.brassgoggledcoders.mccivilizations.api.service;

import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;

public interface ICivilizationRepositoryProvider {
    ICivilizationRepository getCivilizationRepository();

    ILandClaimRepository getLandClaimRepository();
}

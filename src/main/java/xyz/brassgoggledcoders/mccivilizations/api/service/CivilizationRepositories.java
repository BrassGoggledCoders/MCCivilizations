package xyz.brassgoggledcoders.mccivilizations.api.service;

import com.google.common.base.Suppliers;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public class CivilizationRepositories {
    private static final Supplier<ICivilizationRepositoryProvider> SERVICE_PROVIDER = Suppliers.memoize(
            () -> ServiceLoader.load(ICivilizationRepositoryProvider.class)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Failed to Find Civilization Service Provider"))
    );

    public static ILandClaimRepository getLandClaimRepository() {
        return SERVICE_PROVIDER.get().getLandClaimRepository();
    }

    public static ICivilizationRepository getCivilizationRepository() {
        return SERVICE_PROVIDER.get().getCivilizationRepository();
    }
}

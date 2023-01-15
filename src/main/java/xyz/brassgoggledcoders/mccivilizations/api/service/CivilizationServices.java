package xyz.brassgoggledcoders.mccivilizations.api.service;

import com.google.common.base.Suppliers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.IClaimedLand;

import java.util.ServiceLoader;
import java.util.function.Supplier;

public class CivilizationServices {
    private static final Supplier<ICivilizationServiceProvider> SERVICE_PROVIDER = Suppliers.memoize(
            () -> ServiceLoader.load(ICivilizationServiceProvider.class)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Failed to Find Civilization Service Provider"))
    );

    public static IClaimedLand getClaimedLand(@Nullable Level level) {
        return SERVICE_PROVIDER.get().getClaimedLand(level);
    }

    public static ICivilizationRepository getCivilizationService(@Nullable Level level) {
        return SERVICE_PROVIDER.get().getCivilizations(level);
    }

    public static ICivilizationServiceProvider getProvider() {
        return SERVICE_PROVIDER.get();
    }

    private static <T> Supplier<T> createService(Class<T> tClass, String name) {
        return Suppliers.memoize(() -> ServiceLoader.load(tClass)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to Find %s Service".formatted(name)))
        );
    }


}

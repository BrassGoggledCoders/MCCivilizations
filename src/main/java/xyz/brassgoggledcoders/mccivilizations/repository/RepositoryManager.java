package xyz.brassgoggledcoders.mccivilizations.repository;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.location.ILocationRepository;
import xyz.brassgoggledcoders.mccivilizations.civilization.CivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.claim.LandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.location.LocationRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RepositoryManager implements AutoCloseable {
    public static RepositoryManager INSTANCE = null;

    private static final LevelResource REPOSITORY_FOLDER = new LevelResource("data/mccivilizations");
    private final MinecraftServer minecraftServer;
    private final ExecutorService ioExecutor;

    private final CivilizationRepository civilizationRepository;
    private final LandClaimRepository landClaimRepository;
    private final LocationRepository locationRepository;

    public RepositoryManager(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.civilizationRepository = new CivilizationRepository(true);
        this.landClaimRepository = new LandClaimRepository(this.civilizationRepository, true);
        this.locationRepository = new LocationRepository(this.civilizationRepository, true);
        this.ioExecutor = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public ICivilizationRepository getCivilizationRepository() {
        return this.civilizationRepository;
    }

    public ILandClaimRepository getLandClaimRepository() {
        return landClaimRepository;
    }

    public ILocationRepository getLocationRepository() {
        return locationRepository;
    }

    private List<? extends Repository> getRepositories() {
        return List.of(
                this.civilizationRepository,
                this.landClaimRepository,
                this.locationRepository
        );
    }

    public void load() {
        for (Repository repository : this.getRepositories()) {
            File repositoryDirectory = this.minecraftServer.getWorldPath(REPOSITORY_FOLDER)
                    .resolve(repository.getName())
                    .toFile();

            if (repositoryDirectory.exists()) {
                File[] filesToLoad = repositoryDirectory.listFiles(file -> file.isFile() && file.getName().endsWith(".nbt"));
                if (filesToLoad != null) {
                    for (File file : filesToLoad) {
                        try {
                            CompoundTag valueTag = NbtIo.read(file);
                            if (valueTag != null) {
                                repository.deserializeAndInsertValue(valueTag);
                            }
                        } catch (IOException e) {
                            MCCivilizations.LOGGER.error("Failed to read file %s".formatted(file.getName()), e);
                        }
                    }
                }
            }
        }
    }

    public void save() {
        save(false);
    }

    public void save(boolean all) {
        for (Repository repository : this.getRepositories()) {
            if (all || repository.isDirty()) {
                File repositoryDirectory = this.minecraftServer.getWorldPath(REPOSITORY_FOLDER)
                        .resolve(repository.getName())
                        .toFile();

                boolean canSave = repositoryDirectory.exists();
                if (!canSave) {
                    canSave = repositoryDirectory.mkdirs();
                }
                if (canSave) {
                    List<UUID> idsToSave = new ArrayList<>(all ? repository.getIds() : repository.getDirtyIds());
                    for (UUID id : idsToSave) {
                        CompoundTag serializedValue = repository.getSerializedValue(id);
                        Path path = repositoryDirectory.toPath().resolve(id.toString() + ".nbt");
                        this.ioExecutor.execute(new SaveTask(path, serializedValue));
                    }
                    repository.clearDirtyIds();
                }
            }
        }
    }

    public void playerLoggedIn(Player entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            for (Repository repository : this.getRepositories()) {
                repository.onPlayerJoin(serverPlayer);
            }
        }
    }

    @Override
    public void close() {
        this.save();
        this.ioExecutor.shutdown();
        try {
            if (!this.ioExecutor.isTerminated()) {
                MCCivilizations.LOGGER.info("Saving Civilizations data");
                if (!this.ioExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
                    MCCivilizations.LOGGER.error("Failed to finish all tasks while saving Civilizations");
                } else {
                    MCCivilizations.LOGGER.info("Finished saving Civilizations data");
                }
            }
        } catch (InterruptedException e) {
            MCCivilizations.LOGGER.error("Interrupted while saving Civilizations", e);
        }

    }
}

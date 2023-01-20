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
import xyz.brassgoggledcoders.mccivilizations.civilization.CivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.claim.LandClaimRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RepositoryManager {
    public static RepositoryManager INSTANCE = null;

    private static final LevelResource REPOSITORY_FOLDER = new LevelResource("data/mccivilizations");
    private final MinecraftServer minecraftServer;

    private final CivilizationRepository civilizationRepository;
    private final LandClaimRepository landClaimRepository;

    public RepositoryManager(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.civilizationRepository = new CivilizationRepository(true);
        this.landClaimRepository = new LandClaimRepository(this.civilizationRepository, true);
    }

    public ICivilizationRepository getCivilizationRepository() {
        return this.civilizationRepository;
    }

    public ILandClaimRepository getLandClaimRepository() {
        return landClaimRepository;
    }

    private List<? extends Repository> getRepositories() {
        return List.of(
                this.civilizationRepository,
                this.landClaimRepository
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
        for (Repository repository : this.getRepositories()) {
            if (repository.isDirty()) {
                File repositoryDirectory = this.minecraftServer.getWorldPath(REPOSITORY_FOLDER)
                        .resolve(repository.getName())
                        .toFile();

                boolean canSave = repositoryDirectory.exists();
                if (!canSave) {
                    canSave = repositoryDirectory.mkdirs();
                }
                if (canSave) {
                    List<UUID> dirtyIds = new ArrayList<>(repository.getDirtyIds());
                    for (UUID id : dirtyIds) {
                        CompoundTag serializedValue = repository.getSerializedValue(id);
                        Path path = repositoryDirectory.toPath().resolve(id.toString() + ".nbt");
                        if (serializedValue == null) {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                MCCivilizations.LOGGER.error("Failed to delete file %s".formatted(path.toString()), e);
                            }
                        } else {
                            try {
                                NbtIo.write(
                                        serializedValue,
                                        path.toFile()
                                );
                            } catch (IOException e) {
                                MCCivilizations.LOGGER.error("Failed to save file %s's content: %s".formatted(path.toString(), serializedValue.toString()), e);
                            }
                        }

                    }
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
}

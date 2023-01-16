package xyz.brassgoggledcoders.mccivilizations.repository;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.civilization.CivilizationRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class RepositoryManager {
    public static RepositoryManager INSTANCE = null;

    private static final LevelResource REPOSITORY_FOLDER = new LevelResource("mccivilizations");
    private final MinecraftServer minecraftServer;

    private final CivilizationRepository civilizationRepository;

    public RepositoryManager(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.civilizationRepository = new CivilizationRepository();
    }

    public ICivilizationRepository getCivilizationRepository() {
        return this.civilizationRepository;
    }

    private List<? extends Repository> getRepositories() {
        return List.of(
                this.civilizationRepository
        );
    }

    public void load() {
        for (Repository repository : this.getRepositories()) {
            File repositoryDirectory = this.minecraftServer.getWorldPath(REPOSITORY_FOLDER)
                    .resolve(repository.getName())
                    .toFile();

            if (repositoryDirectory.exists()) {
                File[] filesToLoad = repositoryDirectory.listFiles(file -> file.isFile() && file.getName().endsWith(".snbt"));
                if (filesToLoad != null) {
                    for (File file : filesToLoad) {
                        try {
                            CompoundTag valueTag = TagParser.parseTag(Files.readString(file.toPath()));
                            repository.deserializeAndInsertValue(valueTag);
                        } catch (CommandSyntaxException | IOException e) {
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
                    for (Map.Entry<String, CompoundTag> valuesToSave : repository.getSerializedValuesToSave().entrySet()) {
                        String fileContents = valuesToSave.getValue().toString();
                        Path path = repositoryDirectory.toPath().resolve(valuesToSave.getKey());
                        try {
                            Files.writeString(
                                    path,
                                    fileContents,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING
                            );
                        } catch (IOException e) {
                            MCCivilizations.LOGGER.error("Failed to save file %s's content: %s".formatted(path.toString(), fileContents), e);
                        }
                    }
                }
            }
        }
    }
}

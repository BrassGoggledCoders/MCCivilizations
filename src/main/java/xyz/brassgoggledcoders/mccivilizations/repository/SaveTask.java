package xyz.brassgoggledcoders.mccivilizations.repository;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveTask implements Runnable {
    private final Path path;
    private final CompoundTag serializedValue;

    public SaveTask(Path path, @Nullable CompoundTag serializedValue) {
        this.path = path;
        this.serializedValue = serializedValue;
    }

    @Override
    public void run() {
        try {
            if (serializedValue != null) {
                MCCivilizations.LOGGER.debug("Saving file {} with Value {}", path, serializedValue);
                NbtIo.write(
                        serializedValue,
                        path.toFile()
                );
                MCCivilizations.LOGGER.debug("Finished saving file {}", path);
            } else {
                try {
                    MCCivilizations.LOGGER.debug("Deleting file {}", path);
                    Files.delete(path);
                } catch (IOException e) {
                    MCCivilizations.LOGGER.error("Failed to delete file %s".formatted(path.toString()), e);
                }
            }
        } catch (IOException e) {
            MCCivilizations.LOGGER.error("Failed to save file %s's content: %s".formatted(path.toString(), serializedValue.toString()), e);
        }
    }
}

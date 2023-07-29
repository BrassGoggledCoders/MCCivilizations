package xyz.brassgoggledcoders.mccivilizations;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import xyz.brassgoggledcoders.mccivilizations.command.MCCivilizationsCommand;
import xyz.brassgoggledcoders.mccivilizations.compat.naming.NamingCompat;
import xyz.brassgoggledcoders.mccivilizations.content.*;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;

import java.util.function.Supplier;

@Mod(MCCivilizations.MODID)
public class MCCivilizations {

    public static final String MODID = "mccivilizations";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Supplier<Registrate> REGISTRATE = Suppliers.memoize(() -> Registrate.create(MODID)
            .addDataGenerator(ProviderType.ENTITY_TAGS, MCCivilizationsEntityTags::generate)
    );

    public MCCivilizations() {
        MCCivilizationsBlocks.setup();
        MCCivilizationsLocationTypes.setup();
        MCCivilizationsRecipes.setup();
        MCCivilizationsText.setup();

        NetworkHandler.setup();

        runCompat("naming", () -> NamingCompat::setup);

        MinecraftForge.EVENT_BUS.addListener(MCCivilizationsCommand::register);
    }

    public static void runCompat(String modId, Supplier<Runnable> setup) {
        if (ModList.get().isLoaded(modId)) {
            setup.get().run();
        }
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }

    public static Registrate getRegistrate() {
        return REGISTRATE.get();
    }
}

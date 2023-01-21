package xyz.brassgoggledcoders.mccivilizations;

import com.google.common.base.Suppliers;
import com.minerarcana.naming.Naming;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import xyz.brassgoggledcoders.mccivilizations.compat.naming.NamingCompat;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsBlocks;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsRecipes;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;
import xyz.brassgoggledcoders.mccivilizations.network.NetworkHandler;

import java.util.function.Supplier;

@Mod(MCCivilizations.MODID)
public class MCCivilizations {

    public static final String MODID = "mccivilizations";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final Supplier<Registrate> REGISTRATE = Suppliers.memoize(() -> Registrate.create(MODID));

    public MCCivilizations() {
        MCCivilizationsBlocks.setup();
        MCCivilizationsRecipes.setup();
        MCCivilizationsText.setup();

        NetworkHandler.setup();

        runCompat("naming", () -> NamingCompat::setup);
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

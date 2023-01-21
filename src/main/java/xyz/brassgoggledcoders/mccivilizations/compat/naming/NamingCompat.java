package xyz.brassgoggledcoders.mccivilizations.compat.naming;

import com.minerarcana.naming.api.blockentity.NameableBlockEntityWrapper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsBlocks;

import java.util.function.Function;

public class NamingCompat {
    public static void setup() {
        FMLJavaModLoadingContext.get()
                .getModEventBus()
                .addListener(NamingCompat::imcEnqueue);
    }

    public static void imcEnqueue(InterModEnqueueEvent event) {
        InterModComms.sendTo(
                "naming",
                "nameable_blocks",
                () -> Pair.of(
                        MCCivilizationsBlocks.CIVILIZATION_BANNER_BLOCK_ENTITY.get(),
                        (Function<BlockEntity, NameableBlockEntityWrapper<?>>) NameableCivilizationBanner::forBlock
                )
        );
    }
}

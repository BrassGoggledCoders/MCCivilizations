package xyz.brassgoggledcoders.mccivilizations.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Function4;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.Civilization;
import xyz.brassgoggledcoders.mccivilizations.api.civilization.ICivilizationRepository;
import xyz.brassgoggledcoders.mccivilizations.api.claim.ILandClaimRepository;
import xyz.brassgoggledcoders.mccivilizations.api.repositories.CivilizationRepositories;
import xyz.brassgoggledcoders.mccivilizations.content.MCCivilizationsText;

import java.util.function.BiFunction;
import java.util.function.Function;

public class LandClaimCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("land_claim")
                .then(forType("claim", LandClaimCommand::claimPosition))
                .then(forType("unclaim", LandClaimCommand::unclaimPosition));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> forType(
            String name,
            BiFunction<Boolean, Function<CommandContext<CommandSourceStack>, Vec2>, Command<CommandSourceStack>> commandCreator
    ) {
        return Commands.literal(name)
                .then(Commands.literal("admin")
                        .requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_MODERATORS))
                        .then(Commands.argument("offset", Vec2Argument.vec2())
                                .executes(commandCreator.apply(false, context -> Vec2Argument.getVec2(context, "offset")))
                        )
                        .executes(commandCreator.apply(false, LandClaimCommand::getHere))
                )
                .then(Commands.argument("offset", Vec2Argument.vec2())
                        .executes(commandCreator.apply(false, context -> Vec2Argument.getVec2(context, "offset")))
                )
                .executes(commandCreator.apply(false, LandClaimCommand::getHere));
    }

    private static Command<CommandSourceStack> claimPosition(boolean admin, Function<CommandContext<CommandSourceStack>, Vec2> getOffset) {
        return handleClaim(
                admin,
                getOffset,
                (civilization, levelKey, chunkPos, sourceStack) -> {
                    ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();
                    if (landClaimRepository.isClaimed(levelKey, chunkPos)) {
                        sourceStack.sendFailure(MCCivilizationsText.CHUNK_ALREADY_CLAIMED);
                        return false;
                    } else {
                        landClaimRepository.addClaim(civilization, levelKey, chunkPos);
                        sourceStack.sendSuccess(MCCivilizationsText.CHUNK_CLAIMED, true);
                        return true;
                    }
                }
        );
    }

    private static Command<CommandSourceStack> unclaimPosition(boolean admin, Function<CommandContext<CommandSourceStack>, Vec2> getOffset) {
        return handleClaim(
                admin,
                getOffset,
                (civilization, levelKey, chunkPos, sourceStack) -> {
                    ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();
                    if (landClaimRepository.getClaimOwner(levelKey, chunkPos) != civilization) {
                        sourceStack.sendFailure(MCCivilizationsText.CHUNK_NOT_CLAIMED);
                        return false;
                    } else {
                        landClaimRepository.removeClaim(civilization, levelKey, chunkPos);
                        sourceStack.sendSuccess(MCCivilizationsText.CHUNK_UNCLAIMED, true);
                        return true;
                    }
                });
    }

    private static Command<CommandSourceStack> handleClaim(
            boolean admin,
            Function<CommandContext<CommandSourceStack>, Vec2> getOffset,
            Function4<Civilization, ResourceKey<Level>, ChunkPos, CommandSourceStack, Boolean> handler
    ) {
        return context -> {
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

            ICivilizationRepository civilizationRepository = CivilizationRepositories.getCivilizationRepository();
            ILandClaimRepository landClaimRepository = CivilizationRepositories.getLandClaimRepository();

            ResourceKey<Level> levelKey = context.getSource()
                    .getLevel()
                    .dimension();

            Vec2 position = getOffset.apply(context);
            ChunkPos chunkPos = new ChunkPos((int) position.x, (int) position.y);
            Civilization civilization;

            if (admin) {
                civilization = landClaimRepository.getClaimOwner(levelKey, chunkPos);
            } else {
                civilization = civilizationRepository.getCivilizationByCitizen(serverPlayer);
            }

            if (civilization != null) {
                return handler.apply(civilization, levelKey, chunkPos, context.getSource()) ? 1 : 0;
            } else {
                context.getSource().sendFailure(MCCivilizationsText.CITIZENSHIP_REQUIRED);
                return 0;
            }
        };
    }

    private static Vec2 getHere(CommandContext<CommandSourceStack> context) {
        Vec3 vec3 = context.getSource().getPosition();
        return new Vec2((float) vec3.x(), (float) vec3.z());
    }
}

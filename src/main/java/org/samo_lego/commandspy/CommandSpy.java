package org.samo_lego.commandspy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.samo_lego.commandspy.permission.PermissionHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class CommandSpy implements ModInitializer {
    public static final Logger LOGGER = (Logger) LogManager.getLogger("CommandSpy");
    public static final String MODID = "commandspy";
    private static final Path configDirectory = FabricLoader.getInstance().getConfigDir();
    public static boolean luckpermsLoaded;
    public static SpyConfig config;

    @Override
    public void onInitialize() {
        // Info that mod is loading ...
        LOGGER.info("Loading CommandSpy by samo_lego.");
        // Loading config
        config = SpyConfig.loadConfig(new File(configDirectory.toString() + "/CommandSpyConfig.json"));

        luckpermsLoaded = FabricLoader.getInstance().isModLoaded("luckperms");
    }

    /**
     * Determines whether command should be logged, depending on config
     *
     * @param command command string
     * @return true if it should be logged, otherwise false
     */
    public static boolean shouldLog(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        // If command is on the (config) blacklist, it shouldn't be logged to console
        return !config.blacklistedCommands.contains(command.split(" ")[0]);
    }

    /**
     * Logs command to the console
     *
     * @param command    command to log
     * @param source     the command source
     * @param permission the permission to use if LuckPerms is loaded
     */
    public static void logCommand(String command, CommandSourceStack source, String permission) {
        if (config.logging.logToConsole) {
            LOGGER.info(command);
        }

        MutableComponent text = Component.literal(command).withStyle(ChatFormatting.GRAY);
        PlayerList playerManager = source.getServer().getPlayerList();

        if (luckpermsLoaded) {
            // LuckPerms is loaded, so we will make additional permission check

            List<ServerPlayer> players = playerManager.getPlayers();

            players.forEach(player -> {
                if (PermissionHelper.checkPermission(player, permission)) {
                    player.sendSystemMessage(text);
                }
            });
        } else if (config.logging.logToOps) {
            // Vanilla way - all ops get message
            Component message = Component.translatable("chat.type.admin", source.getDisplayName(), text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            for (ServerPlayer serverPlayerEntity : source.getServer().getPlayerList().getPlayers()) {
                if (serverPlayerEntity != source.getPlayer() && source.getServer().getPlayerList().isOp(new NameAndId(serverPlayerEntity.getGameProfile()))) {
                    serverPlayerEntity.sendSystemMessage(message);
                }
            }
        }
    }
}

package org.samo_lego.commandspy.mixin;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.samo_lego.commandspy.CommandSpy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;

import static org.samo_lego.commandspy.CommandSpy.MODID;
import static org.samo_lego.commandspy.CommandSpy.config;


// Mojang: ServerGamePacketListenerImpl
// Yarn: ServerPlayNetworkHandler
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerPlayNetworkHandler {

    @Unique
    private final ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl) (Object) this;


    // Injection for player chatting
    // Mojang: handleChatCommand
    // Yarn: onCommandExecution
    @Inject(
            method = "handleChatCommand",
            at = @At(value = "RETURN")
    )
    private void onCommandExecution(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        boolean enabled = config.logging.logPlayerCommands;
        String command = packet.command();

        if (enabled && CommandSpy.shouldLog(command)) {
            // Message style from config
            String message = config.messages.playerMessage;
            ServerPlayer player = self.getPlayer();

            // Other info, later optionally appended to message
            // Mojang: getScoreboardName
            // Yarn: getNameForScoreboard
            String playername = player.getScoreboardName();
            String uuid = player.getStringUUID();

            // Saving those to hashmap for fancy printing with logger
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("playername", playername);
            valuesMap.put("uuid", uuid);
            valuesMap.put("command", command);
            valuesMap.put("dimension", player.getDefaultDimensions(Pose.STANDING).toString());

            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            // Logging to console
            CommandSpy.logCommand(sub.replace(message), player.createCommandSourceStack(), MODID + ".log.players");
        }
    }
}

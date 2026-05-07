package org.samo_lego.commandspy.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.commandspy.CommandSpy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import static org.samo_lego.commandspy.CommandSpy.MODID;
import static org.samo_lego.commandspy.CommandSpy.config;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin {
    @Shadow
    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, ServerLevel world, BlockPos pos) {
        throw new AssertionError();
    }

    @Inject(
            method = "executeClickCommandsIfPresent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"
            )
    )
    private void catchSignCommand(ServerLevel world, Player player, BlockPos pos, boolean front, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ClickEvent clickEvent) {
        if (config.logging.logSignCommands && clickEvent instanceof ClickEvent.RunCommand(String command)) {

            // Getting message style from config
            String message = CommandSpy.config.messages.signMessage;

            // Getting other info
            String dimension = world.dimension().identifier().toString();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            // Saving those to hashmap for fancy printing with logger
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("dimension", dimension);
            valuesMap.put("command", command);
            valuesMap.put("x", String.valueOf(x));
            valuesMap.put("y", String.valueOf(y));
            valuesMap.put("z", String.valueOf(z));
            StrSubstitutor sub = new StrSubstitutor(valuesMap);

            // Logging to console
            CommandSpy.logCommand(sub.replace(message), createCommandSourceStack(player, world, pos), MODID + ".log.signs");
        }
    }
}
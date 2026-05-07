package org.samo_lego.commandspy.mixin;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.samo_lego.commandspy.CommandSpy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;

import static org.samo_lego.commandspy.CommandSpy.MODID;
import static org.samo_lego.commandspy.CommandSpy.config;


@Mixin(BaseCommandBlock.class)
public abstract class MixinCommandBlockExecutor {

    @Shadow
    public abstract String getCommand();

    @Shadow
    public abstract CommandSourceStack createCommandSourceStack(ServerLevel world, CommandSource output);

    // Injection for command block executing commands
    @Inject(method = "performCommand", at = @At(value = "RETURN"))
    private void execute(ServerLevel world, CallbackInfoReturnable<Boolean> cir) {
        // Checking if mixin should be enabled todo
        boolean enabled = config.logging.logCommandBlockCommands;
        String command = this.getCommand();

        if (enabled && CommandSpy.shouldLog(command)) {
            // Getting other info
            CommandSourceStack source = this.createCommandSourceStack(world, CommandSource.NULL);
            String dimension = world.dimension().identifier().toString();
            int x = (int) (source.getPosition().x - 0.5);
            int y = (int) source.getPosition().y;
            int z = (int) (source.getPosition().z - 0.5);

            // Saving those to hashmap for fancy printing with logger
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("dimension", dimension);
            valuesMap.put("command", command);
            valuesMap.put("x", String.valueOf(x));
            valuesMap.put("y", String.valueOf(y));
            valuesMap.put("z", String.valueOf(z));
            StrSubstitutor sub = new StrSubstitutor(valuesMap);

            // Logging to console
            boolean result = cir.getReturnValue();
            if (result) {
                CommandSpy.logCommand(
                        sub.replace(config.messages.commandBlockSuccessMessage),
                        source,
                        MODID + ".log.command_blocks"
                );
            } else if (!config.logging.logCommandBlockWhenSuccessful) {
                CommandSpy.logCommand(
                        sub.replace(config.messages.commandBlockFailedMessage),
                        source,
                        MODID + ".log.command_blocks"
                );
            }
        }
    }
}
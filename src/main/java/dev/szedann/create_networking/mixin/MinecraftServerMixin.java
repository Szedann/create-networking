package dev.szedann.create_networking.mixin;

import dev.szedann.create_networking.ComputerTicker;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "tickServer", at = @At("TAIL"))
	private void tickServer(CallbackInfo ci) {
		ComputerTicker.tick();
	}
}

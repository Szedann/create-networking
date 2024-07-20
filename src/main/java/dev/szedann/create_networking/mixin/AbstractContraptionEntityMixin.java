package dev.szedann.create_networking.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import com.simibubi.create.content.contraptions.Contraption;

import dan200.computercraft.api.ComputerCraftTags;

//import dev.szedann.create_networking.ComputerTicker;

import dev.szedann.create_networking.ComputerTicker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContraptionEntity.class)
public class AbstractContraptionEntityMixin {
//	@Shadow
//	protected Contraption contraption;
//
//	@Inject(method = "disassemble", at = @At("TAIL"))
//	public void disassemble(CallbackInfo ci) {
//		//TODO: remove old computers
//	}
}

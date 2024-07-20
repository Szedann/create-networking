package dev.szedann.create_networking.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractComputerBlockEntity.class)
public class AbstractComputerBlockEntityMixin {
//	@WrapOperation(method = "setRemoved", at = @At(value = "INVOKE", target = "Ldan200/computercraft/shared/computer/blocks/AbstractComputerBlockEntity;unload()V"))
//	private void setRemoved(AbstractComputerBlockEntity instance, Operation<Void> original) {}
}

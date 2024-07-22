package dev.szedann.create_networking.mixin;


import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractComputerBlockEntity.class)
public class AbstractComputerBlockEntityMixin {
//	@WrapOperation(method = "setRemoved", at = @At(value = "INVOKE", target = "Ldan200/computercraft/shared/computer/blocks/AbstractComputerBlockEntity;unload()V"))
//	private void setRemoved(AbstractComputerBlockEntity instance, Operation<Void> original) {}
}

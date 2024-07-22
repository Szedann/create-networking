package dev.szedann.create_networking;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MonitorMovementBehaviour implements MovementBehaviour {
	@Override
	public boolean renderAsNormalBlockEntity() {
		return true;
	}

	@Override
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
		MovementBehaviour.super.renderInContraption(context, renderWorld, matrices, buffer);

	}
}

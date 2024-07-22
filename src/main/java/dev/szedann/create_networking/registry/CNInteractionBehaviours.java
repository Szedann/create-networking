package dev.szedann.create_networking.registry;

import com.simibubi.create.AllInteractionBehaviours;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.ModRegistry;
import dev.szedann.create_networking.ComputerInteractionBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;


public class CNInteractionBehaviours {
	public static void register() {
		add(ModRegistry.Blocks.COMPUTER_NORMAL.id(), new ComputerInteractionBehaviour());
		add(ModRegistry.Blocks.COMPUTER_ADVANCED.id(), new ComputerInteractionBehaviour());
	}

	private static void add(Block block, MovingInteractionBehaviour behaviour) {
		AllInteractionBehaviours.registerBehaviour(block, behaviour);
	}

	private static void add(ResourceLocation block, MovingInteractionBehaviour behaviour) {
		AllInteractionBehaviours.registerBehaviour(block, behaviour);
	}
}

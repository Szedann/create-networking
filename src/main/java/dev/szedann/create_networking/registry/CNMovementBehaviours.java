package dev.szedann.create_networking.registry;

import com.simibubi.create.AllMovementBehaviours;

import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;

import dan200.computercraft.shared.ModRegistry;
import dev.szedann.create_networking.MonitorMovementBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class CNMovementBehaviours {
	public static void register(){
		add(ModRegistry.Blocks.MONITOR_NORMAL.id(), new MonitorMovementBehaviour());
		add(ModRegistry.Blocks.MONITOR_ADVANCED.id(), new MonitorMovementBehaviour());
		add(ModRegistry.Blocks.WIRELESS_MODEM_NORMAL.id(), new MonitorMovementBehaviour());
		add(ModRegistry.Blocks.WIRELESS_MODEM_ADVANCED.id(), new MonitorMovementBehaviour());
	}

	private static void add(Block block, MovementBehaviour movementBehaviour){
		AllMovementBehaviours.registerBehaviour(block, movementBehaviour);
	}

	private static void add(ResourceLocation resourceLocation, MovementBehaviour movementBehaviour){
		AllMovementBehaviours.registerBehaviour(resourceLocation, movementBehaviour);
	}
}

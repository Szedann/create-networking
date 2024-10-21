package dev.szedann.create_networking;

import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.peripheral.monitor.MonitorRenderer;
import dev.szedann.create_networking.registry.CNInteractionBehaviours;
import dev.szedann.create_networking.registry.CNMovementBehaviours;

public class ModSetup {
	public static void register(){
		CNInteractionBehaviours.register();
		CNMovementBehaviours.register();
		Config.monitorRenderer = MonitorRenderer.TBO;
	}
}

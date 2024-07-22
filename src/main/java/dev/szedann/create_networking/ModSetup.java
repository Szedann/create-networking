package dev.szedann.create_networking;

import dev.szedann.create_networking.registry.CNInteractionBehaviours;
import dev.szedann.create_networking.registry.CNMovementBehaviours;

public class ModSetup {
	public static void register(){
		CNInteractionBehaviours.register();
		CNMovementBehaviours.register();
	}
}

package dev.szedann.create_networking;

import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Pair;

import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.shared.computer.core.ServerComputer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ComputerTicker {
	public static Set<ServerComputer> computers = new HashSet<ServerComputer>();

	public static void registerComputer(ServerComputer computer) {
		computers.add(computer);
	}

	public static void tick() {
		for (ServerComputer computer : computers) {
//			computer.tickServer();
			computer.keepAlive();
		}
	}

}

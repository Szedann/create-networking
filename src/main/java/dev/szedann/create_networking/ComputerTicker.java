package dev.szedann.create_networking;

import dan200.computercraft.shared.computer.core.ServerComputer;
import java.util.HashSet;
import java.util.Set;

public class ComputerTicker {
	public static Set<ServerComputer> computers = new HashSet<>();

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

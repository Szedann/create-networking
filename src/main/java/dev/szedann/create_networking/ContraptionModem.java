package dev.szedann.create_networking;

import dan200.computercraft.api.filesystem.Mount;
import dan200.computercraft.api.filesystem.WritableMount;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import dan200.computercraft.api.peripheral.NotAttachedException;
import dan200.computercraft.api.peripheral.WorkMonitor;
import dan200.computercraft.core.computer.GuardedLuaContext;
import dan200.computercraft.core.methods.PeripheralMethod;
import dan200.computercraft.core.util.LuaUtil;

import dan200.computercraft.shared.computer.core.ServerContext;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ContraptionModem implements IPeripheral {
	private final Map<String, IPeripheral> peripherals;
	private final Map<String, RemotePeripheralWrapper> peripheralWrappers = new HashMap<>();
	private final Level level;

	ContraptionModem(Map<String, IPeripheral> peripherals, Level level){
		this.peripherals = peripherals;
		this.level = level;
	}

	@Override
	public void attach(IComputerAccess computer) {
		IPeripheral.super.attach(computer);

		peripherals.forEach((name, peripheral) -> {
			var methods = ServerContext.get(Objects.requireNonNull(level.getServer())).peripheralMethods().getSelfMethods(peripheral);
			var wrapper = new RemotePeripheralWrapper(this, peripheral, computer, name, methods);
			this.peripheralWrappers.put(name, wrapper);
			wrapper.attach();
		});
	}

	@Override
	public String getType() {
		return "peripheral_hub";
	}

	@Override
	public boolean equals(@Nullable IPeripheral other) {
		return false;
	}

	@LuaFunction
	public final Collection<String> getNamesRemote(){
		return peripheralWrappers.keySet();
	}

	@LuaFunction
	public final boolean isPresentRemote(String name){
		return peripheralWrappers.containsKey(name);
	}

	@LuaFunction
	public final Object[] getTypeRemote(IComputerAccess computer, String name){
		RemotePeripheralWrapper peripheral = peripheralWrappers.get(name);
		if(peripheral == null) return null;
		return LuaUtil.consArray(peripheral.getType(), peripheral.getAdditionalTypes());
	}

	@LuaFunction
	public final boolean hasTypeRemote(IComputerAccess computer, String name, String type){
		RemotePeripheralWrapper peripheral = peripheralWrappers.get(name);
		if(peripheral == null) return false;
		return peripheral.getType().equals(type) || peripheral.getAdditionalTypes().contains(type);
	}

	@LuaFunction
	public final Collection<String > getMethodsRemote(IComputerAccess computer, String name){
		RemotePeripheralWrapper peripheral = peripheralWrappers.get(name);
		if(peripheral == null) return null;
		return peripheral.getMethodNames();
	}

	public Map<String, IPeripheral> getRemotePeripherals() {
		return peripherals;
	}

	@LuaFunction
	public final MethodResult callRemote(IComputerAccess computer, ILuaContext context, IArguments arguments) throws LuaException {
		String remoteName = arguments.getString(0);
		String methodName = arguments.getString(1);
		RemotePeripheralWrapper peripheral = peripheralWrappers.get(remoteName);
		if(peripheral == null) throw new LuaException("No peripheral: " + remoteName);
		return peripheral.callMethod(context, methodName, arguments.drop(2));
	}

	private static class RemotePeripheralWrapper implements IComputerAccess, GuardedLuaContext.Guard {
		private final ContraptionModem contraptionModem;
		private final IPeripheral peripheral;
		private final IComputerAccess computer;
		private final String name;

		private final String type;
		private final Set<String> additionalTypes;
		private final Map<String, PeripheralMethod> methodMap;

		private volatile boolean attached;
		private final Set<String> mounts = new HashSet<>();

		private @Nullable GuardedLuaContext contextWrapper;

		RemotePeripheralWrapper(ContraptionModem contraptionModem, IPeripheral peripheral, IComputerAccess computer, String name, Map<String, PeripheralMethod> methods) {
			this.contraptionModem = contraptionModem;
			this.peripheral = peripheral;
			this.computer = computer;
			this.name = name;

			type = Objects.requireNonNull(peripheral.getType(), "Peripheral type cannot be null");
			additionalTypes = peripheral.getAdditionalTypes();
			methodMap = methods;
		}

		public void attach() {
			attached = true;
			peripheral.attach(this);
			computer.queueEvent("peripheral", getAttachmentName());
		}

		public void detach() {
			peripheral.detach(this);
			computer.queueEvent("peripheral_detach", getAttachmentName());
			attached = false;

			synchronized (this) {
				if (!mounts.isEmpty()) {
					CreateNetworking.LOGGER.warn("Peripheral {} called mount but did not call unmount for {}", peripheral, mounts);
				}

				for (var mount : mounts) computer.unmount(mount);
				mounts.clear();
			}
		}

		public String getType() {
			return type;
		}

		public Set<String> getAdditionalTypes() {
			return additionalTypes;
		}

		public Collection<String> getMethodNames() {
			return methodMap.keySet();
		}

		public MethodResult callMethod(ILuaContext context, String methodName, IArguments arguments) throws LuaException {
			var method = methodMap.get(methodName);
			if (method == null) throw new LuaException("No such method " + methodName);

			// Wrap the ILuaContext. We try to reuse the previous context where possible to avoid allocations.
			var contextWrapper = this.contextWrapper;
			if (contextWrapper == null || !contextWrapper.wraps(context)) {
				contextWrapper = this.contextWrapper = new GuardedLuaContext(context, this);
			}

			return method.apply(peripheral, contextWrapper, this, arguments);
		}

		@Override
		public boolean checkValid() {
			return attached;
		}

		// IComputerAccess implementation

		@Override
		public synchronized @Nullable String mount(String desiredLocation, Mount mount) {
			if (!attached) throw new NotAttachedException();
			var mounted = computer.mount(desiredLocation, mount, name);
			mounts.add(mounted);
			return mounted;
		}

		@Override
		public synchronized @Nullable String mount(String desiredLocation, Mount mount, String driveName) {
			if (!attached) throw new NotAttachedException();
			var mounted = computer.mount(desiredLocation, mount, driveName);
			mounts.add(mounted);
			return mounted;
		}

		@Override
		public synchronized @Nullable String mountWritable(String desiredLocation, WritableMount mount) {
			if (!attached) throw new NotAttachedException();
			var mounted = computer.mountWritable(desiredLocation, mount, name);
			mounts.add(mounted);
			return mounted;
		}

		@Override
		public synchronized @Nullable String mountWritable(String desiredLocation, WritableMount mount, String driveName) {
			if (!attached) throw new NotAttachedException();
			var mounted = computer.mountWritable(desiredLocation, mount, driveName);
			mounts.add(mounted);
			return mounted;
		}

		@Override
		public synchronized void unmount(@Nullable String location) {
			if (!attached) throw new NotAttachedException();
			computer.unmount(location);
			mounts.remove(location);
		}

		@Override
		public int getID() {
			if (!attached) throw new NotAttachedException();
			return computer.getID();
		}

		@Override
		public void queueEvent(String event, @Nullable Object... arguments) {
			if (!attached) throw new NotAttachedException();
			computer.queueEvent(event, arguments);
		}

		@Override
		public WorkMonitor getMainThreadMonitor() {
			if (!attached) throw new NotAttachedException();
			return computer.getMainThreadMonitor();
		}

		@Override
		public String getAttachmentName() {
			if (!attached) throw new NotAttachedException();
			return name;
		}

		@Override
		public Map<String, IPeripheral> getAvailablePeripherals() {
			if (!attached) throw new NotAttachedException();
			synchronized (contraptionModem.getRemotePeripherals()) {
				return Map.copyOf(contraptionModem.getRemotePeripherals());
			}
		}

		@Nullable
		@Override
		public IPeripheral getAvailablePeripheral(String name) {
			if (!attached) throw new NotAttachedException();
			synchronized (contraptionModem.getRemotePeripherals()) {
				return contraptionModem.getRemotePeripherals().get(name);
			}
		}
	}

}

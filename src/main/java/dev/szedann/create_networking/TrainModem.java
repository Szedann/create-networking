package dev.szedann.create_networking;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

import dan200.computercraft.core.util.LuaUtil;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class TrainModem implements IPeripheral {

	private Map<String, IPeripheral> peripherals;

	TrainModem(Map<String, IPeripheral> peripherals){
		this.peripherals = peripherals;
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
		return peripherals.keySet();
	}

	@LuaFunction
	public final boolean isPresentRemote(String name){
		return peripherals.containsKey(name);
	}

	@LuaFunction
	public final Object[] getTypeRemote(IComputerAccess computer, String name){
		IPeripheral peripheral = peripherals.get(name);
		if(peripheral == null) return null;
		return LuaUtil.consArray(peripheral.getType(), peripheral.getAdditionalTypes());
	}

	@LuaFunction
	public final boolean hasTypeRemote(IComputerAccess computer, String name, String type){
		IPeripheral peripheral = peripherals.get(name);
		if(peripheral == null) return false;
		return peripheral.getType().equals(type) || peripheral.getAdditionalTypes().contains(type);
	}

	@LuaFunction
	public final String[] getMethodsRemote(IComputerAccess computer, String name){
		IPeripheral peripheral = peripherals.get(name);
		if(peripheral == null) return null;
		return Arrays.stream(peripheral.getClass().getMethods()).map(Method::getName).toArray(String[]::new);
	}

	@LuaFunction
	public final MethodResult callRemote(IComputerAccess computer, ILuaContext context, IArguments arguments) throws LuaException {
		String remoteName = arguments.getString(0);
		String methodName = arguments.getString(1);
		IPeripheral peripheral = peripherals.get(remoteName);
		if(peripheral == null) throw new LuaException("No peripheral: " + remoteName);
//		return peripheral.getClass().getMethod(methodName, arguments)
		return null;
	}

}

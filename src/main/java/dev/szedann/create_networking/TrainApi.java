package dev.szedann.create_networking;

import com.simibubi.create.AllPackets;
import com.simibubi.create.compat.computercraft.implementation.CreateLuaTable;
import com.simibubi.create.content.trains.entity.Train;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.content.trains.station.TrainEditPacket;
import com.simibubi.create.foundation.utility.Components;

import com.simibubi.create.foundation.utility.StringHelper;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaAPI;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TrainApi implements ILuaAPI {

	public Train train;

	TrainApi(Train train){
		this.train = train;
	}

	@Override
	public String[] getNames() {
		return new String[]{"train"};
	}

	@Nullable
	@Override
	public String getModuleName() {
		return ILuaAPI.super.getModuleName();
	}

	@Override
	public void startup() {
		ILuaAPI.super.startup();
	}

	@Override
	public void update() {
		ILuaAPI.super.update();
	}

	@Override
	public void shutdown() {
		ILuaAPI.super.shutdown();
	}

	@LuaFunction
	public String getName(){
		return train.name.getString();
	}

	@LuaFunction(mainThread = true)
	public final void setName(String name) {
		train.name = Components.literal(name);
		AllPackets.getChannel().sendToClientsInCurrentServer(new TrainEditPacket.TrainEditReturnPacket(train.id, name, train.icon.getId()));
	}

	@LuaFunction
	public final double getSpeed(){
		return train.speed;
	}

	@LuaFunction
	public final boolean hasSchedule(){
		return train.runtime.getSchedule() != null;
	}

	@LuaFunction
	public final CreateLuaTable getSchedule() throws LuaException {
		Schedule schedule = train.runtime.getSchedule();
		if (schedule == null)
			throw new LuaException("train doesn't have a schedule");

		return fromCompoundTag(schedule.write());
	}

	@LuaFunction(mainThread = true)
	public final void setSchedule(IArguments arguments) throws LuaException {
		Schedule schedule = Schedule.fromTag(toCompoundTag(new CreateLuaTable(arguments.getTable(0))));
		boolean autoSchedule = train.runtime.getSchedule() == null || train.runtime.isAutoSchedule;
		train.runtime.setSchedule(schedule, autoSchedule);
	}

	private static @NotNull CreateLuaTable fromCompoundTag(CompoundTag tag) throws LuaException {
		return (CreateLuaTable) fromNBTTag(null, tag);
	}

	private static @NotNull Object fromNBTTag(@Nullable String key, Tag tag) throws LuaException {
		byte type = tag.getId();

		if (type == Tag.TAG_BYTE && key != null && key.equals("Count"))
			return ((NumericTag) tag).getAsByte();
		else if (type == Tag.TAG_BYTE)
			return ((NumericTag) tag).getAsByte() != 0;
		else if (type == Tag.TAG_SHORT || type == Tag.TAG_INT || type == Tag.TAG_LONG)
			return ((NumericTag) tag).getAsLong();
		else if (type == Tag.TAG_FLOAT || type == Tag.TAG_DOUBLE)
			return ((NumericTag) tag).getAsDouble();
		else if (type == Tag.TAG_STRING)
			return tag.getAsString();
		else if (type == Tag.TAG_LIST || type == Tag.TAG_BYTE_ARRAY || type == Tag.TAG_INT_ARRAY || type == Tag.TAG_LONG_ARRAY) {
			CreateLuaTable list = new CreateLuaTable();
			CollectionTag<?> listTag = (CollectionTag<?>) tag;

			for (int i = 0; i < listTag.size(); i++) {
				list.put(i + 1, fromNBTTag(null, listTag.get(i)));
			}

			return list;

		} else if (type == Tag.TAG_COMPOUND) {
			CreateLuaTable table = new CreateLuaTable();
			CompoundTag compoundTag = (CompoundTag) tag;

			for (String compoundKey : compoundTag.getAllKeys()) {
				table.put(
						StringHelper.camelCaseToSnakeCase(compoundKey),
						fromNBTTag(compoundKey, compoundTag.get(compoundKey))
				);
			}

			return table;
		}

		throw new LuaException("unknown tag type " + tag.getType().getName());
	}

	private static @NotNull CompoundTag toCompoundTag(CreateLuaTable table) throws LuaException {
		return (CompoundTag) toNBTTag(null, table.getMap());
	}

	private static @NotNull Tag toNBTTag(@Nullable String key, Object value) throws LuaException {
		if (value instanceof Boolean v)
			return ByteTag.valueOf(v);
		else if (value instanceof Byte || (key != null && key.equals("count")))
			return ByteTag.valueOf(((Number) value).byteValue());
		else if (value instanceof Number v) {
			// If number is numerical integer
			if (v.intValue() == v.doubleValue())
				return IntTag.valueOf(v.intValue());
			else
				return DoubleTag.valueOf(v.doubleValue());

		} else if (value instanceof String v)
			return StringTag.valueOf(v);
		else if (value instanceof Map<?, ?> v && v.containsKey(1.0)) { // List
			ListTag list = new ListTag();
			for (Object o : v.values()) {
				list.add(toNBTTag(null, o));
			}

			return list;

		} else if (value instanceof Map<?, ?> v) { // Table/Map
			CompoundTag compound = new CompoundTag();
			for (Object objectKey : v.keySet()) {
				if (!(objectKey instanceof String compoundKey))
					throw new LuaException("table key is not of type string");

				compound.put(
						// Items serialize their resource location as "id" and not as "Id".
						// This check is needed to see if the 'i' should be left lowercase or not.
						// Items store "count" in the same compound tag, so we can check for its presence to see if this is a serialized item
						compoundKey.equals("id") && v.containsKey("count") ? "id" : StringHelper.snakeCaseToCamelCase(compoundKey),
						toNBTTag(compoundKey, v.get(compoundKey))
				);
			}

			return compound;
		}

		throw new LuaException("unknown object type " + value.getClass().getName());
	}
}

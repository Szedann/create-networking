package dev.szedann.create_networking;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;

import com.simibubi.create.content.trains.entity.CarriageContraption;

import com.simibubi.create.content.trains.entity.Train;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.network.Packet;
import dan200.computercraft.api.network.PacketReceiver;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.computer.blocks.AbstractComputerBlockEntity;
import dan200.computercraft.shared.computer.blocks.ComputerBlock;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.computer.inventory.ViewComputerMenu;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import dan200.computercraft.shared.peripheral.modem.ModemPeripheral;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
import dan200.computercraft.shared.peripheral.monitor.MonitorPeripheral;
import dan200.computercraft.shared.peripheral.monitor.MonitorRenderer;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class ComputerInteractionBehaviour extends MovingInteractionBehaviour {


	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		Level level = player.level();
		if (level.isClientSide())
			return true;

		Contraption contraption = contraptionEntity.getContraption();
		StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(localPos);

		CompoundTag nbt = info.nbt();


		if(nbt == null)
			return false;

		ComputerFamily family = ComputerFamily.NORMAL;
		if(BlockEntity.loadStatic(localPos, info.state(), nbt) instanceof AbstractComputerBlockEntity computerBlockEntity)
			family = computerBlockEntity.getFamily();


		int computerID;
		if(nbt.contains("ComputerId"))
			computerID = nbt.getInt("ComputerId");
		else computerID = ComputerCraftAPI.createUniqueNumberedSaveDir(Objects.requireNonNull(level.getServer()), IDAssigner.COMPUTER);

		ServerComputer computer = ComputerTicker.computers.stream().filter(c->c.getID() == computerID).findFirst().orElse(
				ServerContext.get(Objects.requireNonNull(level.getServer())).registry().get(computerID)
		);

		if(computer == null){
			computer = new ServerComputer((ServerLevel) level, localPos, computerID, null, family, Config.computerTermWidth, Config.computerTermHeight);
			computer.register();
		}


		ServerComputer finalComputer = computer;

		if(!ComputerTicker.computers.contains(computer))
			ComputerTicker.registerComputer(computer);

		nbt.putInt("ComputerId", computerID);

		ComputerState computerState = computer.isOn() ? ComputerState.BLINKING : ComputerState.OFF;

		BlockState state = info.state().setValue(ComputerBlock.STATE, computerState);

		setContraptionBlockData(contraptionEntity, localPos, new StructureTemplate.StructureBlockInfo(localPos, state, nbt));

		ArrayList<Contraption> contraptions = new ArrayList<>();

		// add appropriate APIs

		if(contraption instanceof CarriageContraption carriageContraption){
			CompoundTag contraptionNBT = carriageContraption.entity.serializeNBT();
			Train train = Create.RAILWAYS.trains.get(contraptionNBT.getUUID("TrainId"));
			finalComputer.addAPI(new TrainApi(train));

			contraptions.addAll(train.carriages.stream().map(carriage -> carriage.anyAvailableEntity().getContraption())
					.filter(Objects::nonNull).toList());
		}else{
			contraptions.add(contraption);
		}

		// add peripherals

		Map<String, IPeripheral> peripherals = new HashMap<>();

		ContraptionWorld world = new ContraptionWorld(level, contraption);

//		for(Contraption partContraption : contraptions){
			contraption.getBlocks().forEach((blockPos, structureBlockInfo) -> {
//				if(blockPos.equals(localPos)) return;
				if(structureBlockInfo.nbt() == null) return;
				BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, structureBlockInfo.state(), structureBlockInfo.nbt());

				if (blockEntity == null) return;

				blockEntity.setLevel(world);

				IPeripheral peripheral = PeripheralLookup.get().find(world, blockPos, structureBlockInfo.state(), blockEntity, null);

				if(peripheral == null) return;

//				if(peripheral instanceof MonitorPeripheral monitor)




//				ComputerCraftAPI.getWirelessNetwork(level.getServer()).addReceiver(modem);


				if(blockEntity instanceof MonitorBlockEntity monitor &&
						(monitor.getXIndex() != 0 || monitor.getYIndex() != 0)) return;


				peripherals.put(getPeripheralName(peripheral, peripherals), peripheral);

				finalComputer.setPeripheral(ComputerSide.BOTTOM, peripheral);

			});
//		}

		ContraptionModem modem = new ContraptionModem(peripherals, level);

		finalComputer.setPeripheral(ComputerSide.BACK, modem);


		new ComputerContainerData(computer, new ItemStack(ModRegistry.Items.COMPUTER_NORMAL.get())).open(player, new MenuProvider() {
			@Override
			public @NotNull Component getDisplayName() {
				return Component.translatable("gui.computercraft.view_computer");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
				return new ViewComputerMenu(id, inventory, finalComputer);
			}
		});

		computer.turnOn();

		return true;
	}

	private String getPeripheralName(IPeripheral peripheral, Map<String, IPeripheral> peripherals){
		int maxIndex = peripherals.keySet().stream().filter(key->key.startsWith(peripheral.getType())).mapToInt(peripheralName -> {
			String[] keys = peripheralName.split(Pattern.quote("_"));
			String lastKey = keys[keys.length - 1];
			return Integer.parseInt(lastKey);
		}).max().orElse(-1);
		return peripheral.getType() + "_" + (maxIndex + 1);
	}
}

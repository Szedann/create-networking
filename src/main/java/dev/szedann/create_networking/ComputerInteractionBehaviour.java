package dev.szedann.create_networking;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;

import com.simibubi.create.content.trains.entity.CarriageContraption;

import com.simibubi.create.content.trains.entity.Train;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.ComputerCraftTags;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.computer.blocks.ComputerBlock;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ComputerState;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.computer.inventory.ComputerMenuWithoutInventory;
import dan200.computercraft.shared.computer.inventory.ViewComputerMenu;
import dan200.computercraft.shared.config.Config;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlock;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Objects;

public class ComputerInteractionBehaviour extends MovingInteractionBehaviour {


	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		Level level = player.level();
		if (level.isClientSide())
			return true;

		Contraption contraption = contraptionEntity.getContraption();
		StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(localPos);


		if(info.nbt() == null)
			return false;

		ComputerFamily family = ComputerFamily.NORMAL;

		int computerID;
		if(info.nbt().contains("ComputerId"))
			computerID = info.nbt().getInt("ComputerId");
		else computerID = ComputerCraftAPI.createUniqueNumberedSaveDir(level.getServer(), IDAssigner.COMPUTER);

		ServerComputer computer = ServerContext.get(Objects.requireNonNull(level.getServer())).registry().get(computerID);

		if(computer == null){
			computer = new ServerComputer((ServerLevel) level, localPos, computerID, null, family, Config.computerTermWidth, Config.computerTermHeight);
			computer.register();
		}

		ServerComputer finalComputer = computer;
		new ComputerContainerData(computer, new ItemStack(ModRegistry.Items.COMPUTER_NORMAL.get())).open(player, new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.translatable("gui.computercraft.view_computer");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
				return new ViewComputerMenu(id, inventory, finalComputer);
			}
		});


		if(!ComputerTicker.computers.contains(computer))
			ComputerTicker.registerComputer(computer);

		info.nbt().putInt("ComputerId", computerID);

		info.state().setValue(ComputerBlock.STATE, ComputerState.BLINKING);

		setContraptionBlockData(contraptionEntity, localPos, info);

		contraption.getBlocks().forEach((blockPos, structureBlockInfo) -> {
			if(!structureBlockInfo.state().hasBlockEntity()) return;
		});

		if(contraption instanceof CarriageContraption carriageContraption){
			CompoundTag nbt = carriageContraption.entity.serializeNBT();
			Train train = Create.RAILWAYS.trains.get(nbt.getUUID("TrainId"));
			computer.addAPI(new TrainApi(train));
		}

		return true;
	}
}

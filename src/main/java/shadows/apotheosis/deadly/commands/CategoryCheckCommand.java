package shadows.apotheosis.deadly.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.loot.LootCategory;

public class CategoryCheckCommand {

	public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
		pDispatcher.register(Commands.literal("loot_category").requires(c -> c.hasPermission(2)).executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			ItemStack stack = p.getMainHandItem();
			LootCategory cat = LootCategory.forItem(stack);
			EquipmentSlot[] slots = cat == null ? null : cat.getSlots(stack);
			p.sendMessage(new TextComponent("Loot Category - " + (cat == null ? "null" : cat.name().toLowerCase())), Util.NIL_UUID);
			p.sendMessage(new TextComponent("Equipment Slot - " + (slots == null ? "null" : toStr(slots))), Util.NIL_UUID);
			return 0;
		}));
	}

	static String toStr(EquipmentSlot[] slots) {
		StringBuilder b = new StringBuilder();
		b.append('{');
		for (int i = 0; i < slots.length; i++) {
			b.append(slots[i].name().toLowerCase());
			if (i == slots.length - 1) {
				b.append('}');
			} else b.append(", ");
		}
		return b.toString();
	}

}

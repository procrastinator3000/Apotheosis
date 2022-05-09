package shadows.apotheosis.deadly.affix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

public class AffixHelper {

	public static final String AFFIX_DATA = "AffixData";
	public static final String AFFIXES = "Affixes";

	private static void applyAffix(CompoundTag compound, Affix affix, Tag tag) {
		compound.put(affix.getRegistryName().toString(), tag);
	}

	private static CompoundTag applyAffixes(CompoundTag compound, Map<Affix, Tag> affixes){
		affixes.forEach((a, t) -> applyAffix(compound, a, t));
		return compound;
	}

	/**
	 * Adds this specific affix to the Item's NBT tag.
	 * Disallows illegal affixes.
	 */
	public static void applyAffix(ItemStack stack, Affix affix, Tag tag) {
		LootCategory cat = LootCategory.forItem(stack);
		if (!(stack.getItem() instanceof IAffixSensitiveItem) && (cat == null || !affix.canApply(cat))) return;
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		if (!afxData.contains(AFFIXES))
			afxData.put(AFFIXES, new CompoundTag());
		applyAffix(afxData.getCompound(AFFIXES), affix, tag);
	}

	public static void applyAffix(AbstractArrow arrow, Affix affix, Tag tag) {
		if(!arrow.getPersistentData().contains(AFFIX_DATA))
			arrow.getPersistentData().put(AFFIX_DATA, new CompoundTag());
		CompoundTag afxData = arrow.getPersistentData().getCompound(AFFIX_DATA);
		if(!afxData.contains(AFFIXES))
			afxData.put(AFFIXES, new CompoundTag());
		applyAffix(afxData.getCompound(AFFIXES), affix, tag);
	}

	public static void setAffixes(ItemStack stack, Map<Affix, Tag> affixes) {
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		afxData.put(AFFIXES, applyAffixes(new CompoundTag(), affixes));
	}

	public static void setAffixes(AbstractArrow arrow, Map<Affix, Tag> affixes) {
		if(!arrow.getPersistentData().contains(AFFIX_DATA))
			arrow.getPersistentData().put(AFFIX_DATA, new CompoundTag());
		CompoundTag afxData = arrow.getPersistentData().getCompound(AFFIX_DATA);
		afxData.put(AFFIXES, applyAffixes(new CompoundTag(), affixes));
	}

	public static Map<Affix, Tag> getAffixes(ItemStack stack) {
		Map<Affix, Tag> map = new HashMap<>();
		CompoundTag afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData != null && afxData.contains(AFFIXES)) {
			CompoundTag affixes = afxData.getCompound(AFFIXES);
			affixes.getAllKeys().forEach(key -> {
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				if (affix != null)
					map.put(affix, affixes.get(key));
			});
		}
		return map;
	}

	public static Map<Affix, Tag> getAffixes(AbstractArrow arrow) {
		Map<Affix, Tag> map = new HashMap<>();
		if(!arrow.getPersistentData().contains(AFFIX_DATA))
			return map;
		CompoundTag afxData = arrow.getPersistentData().getCompound(AFFIX_DATA);
		if (afxData.contains(AFFIXES)) {
			CompoundTag affixes = afxData.getCompound(AFFIXES);
			affixes.getAllKeys().forEach(key -> {
				Affix affix = Affix.REGISTRY.getValue(new ResourceLocation(key));
				if (affix != null)
					map.put(affix, affixes.get(key));
			});
		}
		return map;
	}

	public static boolean hasAffixes(ItemStack stack) {
		var afxData = stack.getTagElement(AFFIX_DATA);
		return afxData != null && !afxData.getCompound(AFFIXES).isEmpty();
	}

	public static boolean hasAffixes(AbstractArrow arrow) {
		return !arrow.getPersistentData().getCompound(AFFIX_DATA).getCompound(AFFIXES).isEmpty();
	}

	public static void addLore(ItemStack stack, Component lore) {
		CompoundTag display = stack.getOrCreateTagElement("display");
		ListTag tag = display.getList("Lore", 8);
		tag.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
		display.put("Lore", tag);
	}

	public static List<Affix> getAffixesFor(LootCategory type, LootRarity maxAffixRarity) {
		List<Affix> affixes = new ArrayList<>();
		Affix.REGISTRY.getValues().stream().filter(t -> t.canApply(type) && t.getRarity().ordinal() <= maxAffixRarity.ordinal()).forEach(affixes::add);
		return affixes;
	}

	public static void setRarity(ItemStack stack, LootRarity rarity) {
		Component comp = new TranslatableComponent("%s", new TextComponent("")).withStyle(Style.EMPTY.withColor(rarity.getColor()));
		CompoundTag afxData = stack.getOrCreateTagElement(AFFIX_DATA);
		afxData.putString("Name", Component.Serializer.toJson(comp));
		if (!stack.getOrCreateTagElement("display").contains("Lore")) AffixHelper.addLore(stack, new TranslatableComponent("info.apotheosis.affix_item").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false)));
		afxData.putString("Rarity", rarity.name());
		stack.hideTooltipPart(TooltipPart.MODIFIERS);
	}

	@Nullable
	public static LootRarity getRarity(ItemStack stack) {
		var afxData = stack.getTagElement(AFFIX_DATA);
		if (afxData != null) {
			try {
				return LootRarity.valueOf(afxData.getString("Rarity"));
			} catch (IllegalArgumentException e) {
				afxData.remove("Rarity");
				return null;
			}
		}
		return null;
	}

}
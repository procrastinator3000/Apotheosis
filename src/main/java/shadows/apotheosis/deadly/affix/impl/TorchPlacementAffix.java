package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import shadows.apotheosis.deadly.affix.IntAffixConfig;
import shadows.apotheosis.deadly.config.DeadlyConfig;
import shadows.apotheosis.deadly.loot.LootCategory;

/**
 * Allows the user to place torches from the tool, for a durability cost.
 */
public class TorchPlacementAffix extends IntAffix {

	protected final int maxPlacementCost;
	public TorchPlacementAffix(IntAffixConfig config) {
		super(config);
		this.maxPlacementCost = Math.max(config.getMergeHandler().getMaxValue(), config.getValueProvider().getMaxValue()) +1;
	}

	@Override
	public boolean canApply(LootCategory lootCategory) { return lootCategory == LootCategory.BREAKER; }

	@Override
	public boolean isPrefix() {
		return false;
	}

	@Override
	public InteractionResult onItemUse(UseOnContext ctx, Tag tag) {
		Player player = ctx.getPlayer();
		if(player == null)
			return null;

		if (DeadlyConfig.torchItem.get().useOn(ctx).consumesAction()) {
			if (ctx.getItemInHand().isEmpty())
				ctx.getItemInHand().grow(1);
			player.getItemInHand(ctx.getHand()).hurtAndBreak(getPlacementCost(tag), player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
			return InteractionResult.SUCCESS;
		}

		return null;
	}

	protected int getPlacementCost(Tag tag) {
		int lvl = getIntOrDefault(tag, 0);
		return Math.max(1, maxPlacementCost - lvl);
	}

	@Override
	public String displayValue(Tag tag) {
		return super.displayValue(IntTag.valueOf(getPlacementCost(tag)));
	}
}
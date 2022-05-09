package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.util.random.Weight;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import shadows.apotheosis.deadly.affix.IntAffixConfig;
import shadows.apotheosis.deadly.affix.impl.FloatAffix;
import shadows.apotheosis.deadly.affix.impl.IntAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Targets more than 30 blocks away take additional damage.
 */
public class SnipeDamageAffix extends IntAffix {
	private static final IntAffixConfig DEFAULT_CONFIG =  new IntAffixConfig(
			LootRarity.COMMON,
			Weight.of(3),
			UniformInt.of(2, 10),
			IntAffixConfig.MergeHandler.of(1, 15, IntAffixConfig.MergeFunction.ADD_HALF));

	public SnipeDamageAffix(IntAffixConfig config) {
		super(config);
	}

	public SnipeDamageAffix() { this(DEFAULT_CONFIG); }

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type, Tag tag) {
		Entity shooter = arrow.getOwner();
		if (shooter != null && type == HitResult.Type.ENTITY) {
			if (shooter.distanceToSqr(((EntityHitResult) res).getEntity()) > 30 * 30) {
				arrow.setBaseDamage(arrow.getBaseDamage() + getIntOrDefault(tag, 0));
			}
		}
	}
}
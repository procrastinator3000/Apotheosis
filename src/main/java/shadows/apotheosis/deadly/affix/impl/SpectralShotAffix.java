package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import shadows.apotheosis.deadly.affix.FloatAffixConfig;
import shadows.apotheosis.deadly.affix.impl.FloatAffix;
import shadows.apotheosis.deadly.loot.LootCategory;

import java.util.function.Consumer;

/**
 * Ranged Spectral Shot Affix. Has a chance to fire an additional spectral arrow when shooting.
 */
public class SpectralShotAffix extends FloatAffix {

	public SpectralShotAffix(FloatAffixConfig config) {
		super(config);
	}

	@Override
	public boolean canApply(LootCategory lootCategory) {
		return lootCategory.isRanged();
	}

	@Override
	public void onArrowFired(LivingEntity user, AbstractArrow arrow, ItemStack bow, Tag tag) {
		if (user.level.random.nextFloat() < getFloatOrDefault(tag, 0F)) {
			if (!user.level.isClientSide) {
				ArrowItem arrowitem = (ArrowItem) Items.SPECTRAL_ARROW;
				AbstractArrow spectralArrow = arrowitem.createArrow(user.level, ItemStack.EMPTY, user);
				spectralArrow.shoot(user.getXRot(), user.getYRot(), 0.0F, 1 * 3.0F, 1.0F);
				this.cloneMotion(arrow, spectralArrow);
				spectralArrow.setCritArrow(arrow.isCritArrow());
				spectralArrow.setBaseDamage(arrow.getBaseDamage());
				spectralArrow.setKnockback(arrow.knockback);
				spectralArrow.setRemainingFireTicks(arrow.getRemainingFireTicks());
				spectralArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
				spectralArrow.getPersistentData().putBoolean("apoth.generated", true);
				arrow.level.addFreshEntity(spectralArrow);
			}
		}
	}

	@Override
	public boolean isPrefix() {
		return true;
	}

	@Override
	public void addInformation(Tag tag, Consumer<Component> list) {
		super.addInformation(tag, list, true);
	}

	@Override
	public Component getDisplayName(Tag tag) {
		return super.getDisplayName(tag, true);
	}

	private void cloneMotion(AbstractArrow src, AbstractArrow dest) {
		dest.setDeltaMovement(src.getDeltaMovement().scale(1));
		dest.setYRot(src.getYRot());
		dest.setXRot(src.getXRot());
		dest.yRotO = dest.getYRot();
		dest.xRotO = dest.getXRot();
	}

}
package shadows.apotheosis.deadly.affix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.loot.LootCategory;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public class AttributeAffix extends Affix {

	protected final @Nullable Predicate<LootCategory> types;
	protected final ModifierInst modifier;
	protected final boolean isPrefix;
	protected final AttributeAffixConfig.MergeHandler mergeHandler;

	protected AttributeAffix(AttributeAffixConfig config, boolean isPrefix, @Nullable Predicate<LootCategory> types) {
		super(config);
		this.types = types;
		this.isPrefix = isPrefix;
		this.modifier = getModifierInstance(config);
		this.mergeHandler = config.getMergeHandler();
	}

	@Override
	public boolean isPrefix() {
		return this.isPrefix;
	}

	/**
	 * AttributeAffixes should be handled in {@link shadows.apotheosis.deadly.asm.DeadlyHooks#onEntityDamaged}
	 */
	@Override
	public final void onEntityDamaged(LivingEntity user, @Nullable Entity target, Tag tag) {
		super.onEntityDamaged(user, target, tag);
	}

	@Override
	public Tag merge(Tag instance1, Tag instance2) {
		return IntTag.valueOf(mergeHandler.mergeValues(getInt(instance1), getInt(instance2)));
	}

	@Override
	public void addModifiers(ItemStack stack, Tag tag, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
		LootCategory cat = LootCategory.forItem(stack);
		if (cat == null) {
			DeadlyModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.getRegistryName(), stack.getHoverName());
			return;
		}

		int lvl = getInt(tag);
		if(lvl <= 0)
			return;

		for (EquipmentSlot slot : cat.getSlots(stack)) {
			if (slot == type) {
				map.accept(modifier.attr, modifier.build(slot, this.getRegistryName(), lvl));
			}
		}
	}

	protected static int getInt(Tag tag){
		if(tag.getId() != Tag.TAG_INT)
			return 0;
		return ((IntTag)tag).getAsInt();
	}

	@Override
	public boolean canApply(LootCategory type) {
		return this.types == null || this.types.test(type);
	}

	protected ModifierInst getModifierInstance(AttributeAffixConfig config){
		return new ModifierInst(config.getAttribute(), config.getOperation(), config.valueMapper, new HashMap<>());
	}

	public record ModifierInst(Attribute attr, Operation op, Function<Integer, Float> valueFactory, Map<EquipmentSlot, UUID> cache) {

		public AttributeModifier build(EquipmentSlot slot, ResourceLocation id, int level) {
			return new AttributeModifier(cache.computeIfAbsent(slot, k -> UUID.randomUUID()), "affix:" + id, valueFactory.apply(level), op);
		}
	}

	public static Builder builder(AttributeAffixConfig config) {
		return new Builder(config);
	}

	public static class Builder {

		protected final AttributeAffixConfig config;
		protected Predicate<LootCategory> types;
		protected boolean isPrefix;

		protected Builder(AttributeAffixConfig config) {
			this.config = config;
		}


		public Builder types(Predicate<LootCategory> types) {
			this.types = types;
			return this;
		}

		public Builder setPrefix(boolean isPrefix) {
			this.isPrefix = isPrefix;
			return this;
		}

		public AttributeAffix build() {
			return new AttributeAffix(config, isPrefix, types);
		}

	}

}
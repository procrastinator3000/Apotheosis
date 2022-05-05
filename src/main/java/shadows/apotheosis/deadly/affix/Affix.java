package shadows.apotheosis.deadly.affix;

import java.util.*;
import java.util.function.*;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.checkerframework.dataflow.qual.Pure;
import shadows.apotheosis.deadly.affix.impl.OneFloatAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.ench.asm.EnchHooks;
import shadows.apotheosis.util.Weighted;
import shadows.placebo.config.Configuration;

/**
 * An affix is a construct very similar to an enchantment.
 * However, they are only available via loot, and have some additional rules.
 */
public abstract class Affix extends Weighted implements IForgeRegistryEntry<Affix> {
	public CompoundTag getAsTag(){
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(this.getRegistryName().toString(), true);
		return tag;
	}

//	private static final Map<String, Codec<? extends AffixInstanceData>> CODECS = new HashMap<>();
//
//	protected static Codec<?> registerCodec(ResourceLocation key, Codec<? extends AffixInstanceData> codec){
//		return CODECS.put(key.toString(), codec);
//	}
//
//	protected static Optional<Codec<? extends AffixInstanceData>> getCodec(String key){
//		if(CODECS.containsKey(key))
//			return Optional.of(CODECS.get(key));
//		else
//			return Optional.empty();
//	}

	public static ForgeRegistry<Affix> REGISTRY;

	public static Configuration config;

	/**
	 * The registry name of this item.
	 */
	protected ResourceLocation name;

	//pc3k: in case rarity on affix was meant to be its weight -> for the love of god pls no.
	// It kills possibility of fine-tuning for no reason
	/**
	 * Minimum item rarity required for this affix.
	 */
	protected final LootRarity rarity;

	public abstract boolean isPrefix();

	public Affix(AffixConfig config) {
		super(config.getWeight());
		this.rarity = config.getMinRarity();
	}

	/**
	 * Apply modifiers from this affix to the itemstack's modifiers map.
	 * @param tag NbtTag of affix instance data.
	 * @param type The slot type for modifiers being gathered.
	 * @param map The destination for generated attribute modifiers.
	 */
	public void addModifiers(CompoundTag tag, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
	}

	/**
	 * Adds all tooltip data from this affix to the given stack's tooltip list.
	 * This consumer will insert tooltips immediately after enchantment tooltips, or after the name if none are present.
	 * @param tag NbtTag of affix instance data.
	 * @param list The destination for tooltips.
	 */
	public void addInformation(CompoundTag tag, Consumer<Component> list) {
		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level)));
	}

	/**
	 * Chain the name of this affix to the existing name.  If this is a prefix, it should be applied to the front.
	 * If this is a suffix, it should be applied to the back.
	 * @param name The current name, which may have been modified by other affixes.	
	 * @return The new name, consuming the old name in the process.
	 */
	public Component chainName(Component name, boolean prefix) {
		if (prefix) return new TranslatableComponent("%s %s", new TranslatableComponent("affix." + this.name), name);
		return new TranslatableComponent("%s %s", name, new TranslatableComponent("affix." + this.name));
	}

	/**
	 * Calculates the protection value of this affix, with respect to the given damage source.<br>
	 * Math is in {@link CombatRules#getDamageAfterMagicAbsorb}<br>
	 * Ench module overrides with {@link EnchHooks#getDamageAfterMagicAbsorb}<br>
	 * @param level The level of this affix, if applicable.<br>
	 * @param source The damage source to compare against.<br>
	 * @return How many protection points this affix is worth against this source.<br>
	 */
	@Pure
	public int getProtectionLevel(CompoundTag tag, DamageSource source) {
		return 0;
	}

	/**
	 * Calculates the additional damage this affix deals.
	 * This damage is dealt as player physical damage, and is not impacted by critical strikes.
	 */
	public float getExtraDamageFor(CompoundTag tag, MobType creatureType) {
		return 0.0F;
	}

	/**
	 * Called when someone attacks an entity with an item containing this affix.
	 * More specifically, this is invoked whenever the user attacks a target, while having an item with this affix in either hand or any armor slot.
	 * @param user The wielder of the weapon.  The weapon stack will be in their main hand.
	 * @param target The target entity being attacked.
	 * @param level The level of this affix, if applicable.
	 */
	public void onEntityDamaged(LivingEntity user, @Nullable Entity target, CompoundTag tag) {
	}

	/**
	 * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be
	 * called.
	 */
	public void onUserHurt(LivingEntity user, @Nullable Entity attacker, CompoundTag tag) {
	}

	/**
	 * Called when a user fires an arrow from a bow or crossbow with this affix on it.
	 */
	public void onArrowFired(LivingEntity user, AbstractArrow arrow, ItemStack bow, CompoundTag tag) {
	}

	/**
	 * Called when {@link Item#useOn(UseOnContext)}} would be called for an item with this affix.
	 * Return null to not impact the original result type.
	 */
	@Nullable
	public InteractionResult onItemUse(UseOnContext ctx, CompoundTag tag) {
		return null;
	}

	/**
	 * Called when an arrow that was marked with this affix hits a target.
	 */
	public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type, CompoundTag tag) {
	}

	/**
	 * Called when a shield with this affix blocks some amount of damage.
	 * @param entity The blocking entity.
	 * @param stack  The shield itemstack the affix is on .
	 * @param source The damage source being blocked.
	 * @param amount The amount of damage blocked.
	 * @param level  The level of this affix.
	 * @return	     The amount of damage that is *actually* blocked by the shield, after this affix applies.
	 */
	public float onShieldBlock(LivingEntity entity, ItemStack stack, DamageSource source, float amount, CompoundTag tag) {
		return amount;
	}

	@Override
	public Affix setRegistryName(ResourceLocation name) {
		if (this.name == null) this.name = name;
		return this;
	}

	public Affix setRegistryName(String name) {
		return this.setRegistryName(GameData.checkPrefix(name, false));
	}

	@Override
	public ResourceLocation getRegistryName() {
		return this.name;
	}

	@Override
	public Class<Affix> getRegistryType() {
		return Affix.class;
	}

	public static void classload() {
	}

	@Override
	public String toString() {
		return String.format("Affix: %s", this.name);
	}

	public abstract boolean canApply(LootCategory type);

	public static MutableComponent loreComponent(String text, Object... args) {
		return new TranslatableComponent(text, args).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE);
	}

	public static String fmt(float f) {
		if (f == (long) f) return String.format("%d", (long) f);
		else return String.format("%.2f", f);
	}

	public Component getDisplayName(CompoundTag tag) {
		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", fmt(level)).withStyle(ChatFormatting.GRAY);
	}

	public LootRarity getRarity() {
		return this.rarity;
	}


	public static class AffixInstanceData {

		public static final Codec<AffixInstanceData> CODEC = RecordCodecBuilder.create((instance) -> {
			return instance.group(
					ResourceLocation.CODEC.fieldOf("key").forGetter(AffixInstanceData::getKey)
			).apply(instance, AffixInstanceData::new);
		});
		private final ResourceLocation key;

		public AffixInstanceData(ResourceLocation key) {
			this.key = key;
		}

		public ResourceLocation getKey() {
			return this.key;
		}

		public ResourceLocation key() {
			return key;
		}
	}
}



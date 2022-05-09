package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.valueproviders.FloatProvider;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.FloatAffixConfig;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Consumer;

public abstract class FloatAffix extends Affix {

	protected final FloatAffixConfig.MergeHandler mergeHandler;
	protected final FloatProvider valueProvider;

	public FloatAffix(FloatAffixConfig config) {
		super(config);
		this.valueProvider = config.getValueProvider();
		this.mergeHandler = config.getMergeHandler();
	}

	@Override
	public Tag generate(Random random, @Nullable AffixModifier modifier) {
		float lvl = valueProvider.sample(random);
		if (modifier != null)
			lvl = modifier.editLevel(lvl, valueProvider.getMinValue(), valueProvider.getMaxValue());
		return FloatTag.valueOf(lvl);
	}

	@Override
	public Tag merge(Tag t1, Tag t2) {
		if(t1.getId() != Tag.TAG_FLOAT || t2.getId() != Tag.TAG_FLOAT){
			DeadlyModule.LOGGER.error("Invalid value of tag passed to merge");
			return t1;
		}
		return FloatTag.valueOf(mergeHandler.mergeValues(((FloatTag)t1).getAsFloat(), ((FloatTag)t1).getAsFloat()));
	}

	public static float getFloatOrDefault(@Nonnull Tag tag, float defaultValue) {
		if(tag.getId() == Tag.TAG_FLOAT)
			return ((FloatTag)tag).getAsFloat();

		DeadlyModule.LOGGER.error("Expected float value in tag, got [{}], with idType: {}", tag.getAsString(), tag.getId());
		return defaultValue;
	}

	@Override
	public Component getDisplayName(Tag tag) {
		return getDisplayName(tag, false);
	}

	@Override
	public void addInformation(Tag tag, Consumer<Component> list) {
		addInformation(tag, list, false);
	}

	public Component getDisplayName(Tag tag, boolean moveDecimals) {
		String level;
		if(moveDecimals)
			level = tag.getId() == Tag.TAG_FLOAT ? fmt.format(getFloatOrDefault(tag, 0F) * 100) : "???";
		else
			level = tag.getId() == Tag.TAG_FLOAT ? fmt.format(getFloatOrDefault(tag, 0F)) : "???";

		return new TranslatableComponent("affix." + this.getRegistryName() + ".name", level).withStyle(ChatFormatting.GRAY);
	}

	public void addInformation(Tag tag, Consumer<Component> list, boolean moveDecimals) {
		String level;
		if(moveDecimals)
			level = tag.getId() == Tag.TAG_FLOAT ? fmt.format(getFloatOrDefault(tag, 0F) * 100) : "???";
		else
			level = tag.getId() == Tag.TAG_FLOAT ? fmt.format(getFloatOrDefault(tag, 0F)) : "???";

		list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", level));
	}
}
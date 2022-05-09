package shadows.apotheosis.deadly.affix.impl;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.valueproviders.IntProvider;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.IntAffixConfig;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class IntAffix extends Affix {
    protected final IntAffixConfig.MergeHandler mergeHandler;
    protected final IntProvider valueProvider;

    public IntAffix(IntAffixConfig config) {
        super(config);
        this.valueProvider = config.getValueProvider();
        this.mergeHandler = config.getMergeHandler();
    }

    @Override
    public Tag generate(Random random, @Nullable AffixModifier modifier) {
        int lvl = valueProvider.sample(random);
        if (modifier != null)
            lvl = modifier.editLevel(lvl, valueProvider.getMinValue(), valueProvider.getMaxValue());
        return IntTag.valueOf(lvl);
    }

    @Override
    public Tag merge(Tag t1, Tag t2) {
        if(t1.getId() != Tag.TAG_INT || t2.getId() != Tag.TAG_INT) {
            DeadlyModule.LOGGER.error("Invalid value of tag passed to merge, expected 2 ints but got [{}] and [{}]", t1.getAsString(), t2.getAsString());
            return t1;
        }
        return IntTag.valueOf(mergeHandler.mergeValues(getIntOrDefault(t1, 0), getIntOrDefault(t2, 0)));
    }

    public static int getIntOrDefault(Tag tag, int defaultValue) {
        if(tag.getId() == Tag.TAG_INT)
            return ((IntTag)tag).getAsInt();

        DeadlyModule.LOGGER.error("Expected int value in tag, got [{}], with idType: {}", tag.getAsString(), tag.getId());
        return defaultValue;
    }
}

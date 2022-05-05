package apotheosis;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.deadly.DeadlyLoot;
import shadows.apotheosis.deadly.affix.AffixConfig;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.gen.SpawnerItem;
import shadows.apotheosis.deadly.loot.LootController;
import shadows.apotheosis.deadly.loot.LootRarity;
//import shadows.apotheosis.util.DiscreteFloatProvider;
import shadows.apotheosis.util.SpawnerStats;

import java.sql.Wrapper;
import java.util.List;


@PrefixGameTestTemplate(false)
@GameTestHolder(Apotheosis.MODID)
public class AffixTests {
    static final Logger LOGGER = LogManager.getLogger("Apotheosis:AffixTests");

    @BeforeEach
    public void setup(){

    }

    @GameTest(template = "empty3x3x3")
    public static void swordReceiveAffixes(GameTestHelper helper){
        try {
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            LootController.lootifyItem(sword, LootRarity.EPIC, helper.getLevel().getRandom());
            helper.succeedIf(() ->  {
                if(AffixHelper.getAffixes(sword).size() > 0)
                    helper.succeed();
            });
        } catch (RuntimeException ex){
            throw new GameTestAssertException("Runtime exception during test: " + ex.getMessage());
        }
    }

//    @GameTest(template = "empty3x3x3")
//    public static void floatProviderCodecTests(GameTestHelper helper){
//        var floatAffixConfig = new AffixConfig.DiscreteFloatValue(LootRarity.EPIC, 5, DiscreteFloatProvider.of(List.of(0.1F, 0.2F, 0.3F, 0.4F, 0.5F, 0.6F, 0.7F, 0.8F)));
//        var serialized = AffixConfig.DiscreteFloatValue.CODEC.encodeStart(JsonOps.INSTANCE, floatAffixConfig).result().map(JsonElement::toString).orElse("failed");
//
//        if(serialized.equals("failed"))
//            helper.fail("AffixConfig serializes as 'failed'");
//
//        var convertedBack = AffixConfig.DiscreteFloatValue.CODEC.decode(JsonOps.INSTANCE, new JsonStreamParser(serialized).next()).result().map(Pair::getFirst).orElse(null);
//        if(convertedBack == null)
//            helper.fail("Value after conversion back is null");
//
//        helper.succeed();
//    }




    @Test
    public void foo(){
        Assertions.assertTrue(true);
    }

}

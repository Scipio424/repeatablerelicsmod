package repeatablerelicsmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import repeatablerelicsmod.repetablerelics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpirePatch(clz = AbstractDungeon.class, method = "update")
public class RelicPoolPatch {
    private static final Logger logger = LogManager.getLogger(repetablerelics.MOD_ID);

    public static void Postfix() {
        // Ensure the logic only runs during an active dungeon run
        if (!AbstractDungeon.isPlayerInDungeon()) {
            return;
        }

        // Dynamically check and refresh relic pools during gameplay
        if (!repetablerelics.refreshingPools.contains("common") && repetablerelics.allRelicsSeenInPool(AbstractDungeon.commonRelicPool)) {
            logger.info("All common relics have been seen. Refreshing common relic pool...");
            repetablerelics.refreshRelicPool(AbstractDungeon.commonRelicPool, "common");
        }
        if (!repetablerelics.refreshingPools.contains("uncommon") && repetablerelics.allRelicsSeenInPool(AbstractDungeon.uncommonRelicPool)) {
            logger.info("All uncommon relics have been seen. Refreshing uncommon relic pool...");
            repetablerelics.refreshRelicPool(AbstractDungeon.uncommonRelicPool, "uncommon");
        }
        if (!repetablerelics.refreshingPools.contains("rare") && repetablerelics.allRelicsSeenInPool(AbstractDungeon.rareRelicPool)) {
            logger.info("All rare relics have been seen. Refreshing rare relic pool...");
            repetablerelics.refreshRelicPool(AbstractDungeon.rareRelicPool, "rare");
        }
        if (!repetablerelics.refreshingPools.contains("shop") && repetablerelics.allRelicsSeenInPool(AbstractDungeon.shopRelicPool)) {
            logger.info("All shop relics have been seen. Refreshing shop relic pool...");
            repetablerelics.refreshRelicPool(AbstractDungeon.shopRelicPool, "shop");
        }
        if (!repetablerelics.refreshingPools.contains("boss") && repetablerelics.allRelicsSeenInPool(AbstractDungeon.bossRelicPool)) {
            logger.info("All boss relics have been seen. Refreshing boss relic pool...");
            repetablerelics.refreshRelicPool(AbstractDungeon.bossRelicPool, "boss");
        }
    }
}

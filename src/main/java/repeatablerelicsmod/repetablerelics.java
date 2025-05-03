package repeatablerelicsmod;

import basemod.BaseMod;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import repeatablerelicsmod.util.KeywordInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpireInitializer
public class repetablerelics implements
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber,
        PostDungeonInitializeSubscriber,
        PostUpdateSubscriber {

    public static final String MOD_ID = "repeatablerelicsmod";
    public static final Logger logger = LogManager.getLogger(MOD_ID);

    private static final Set<String> seenRelics = new HashSet<>();
    public static final Set<String> refreshingPools = new HashSet<>(); // Tracks pools currently being refreshed

    private static List<String> commonRelics = Collections.emptyList();
    private static List<String> uncommonRelics = Collections.emptyList();
    private static List<String> rareRelics = Collections.emptyList();
    private static List<String> shopRelics = Collections.emptyList();
    private static List<String> bossRelics = Collections.emptyList();

    public static String makeID(String id) {
        return MOD_ID + ":" + id;
    }

    public static void initialize() {
        logger.info("Initializing repetablerelicsmod...");
        new repetablerelics();
    }

    public repetablerelics() {
        logger.info("Subscribing repetablerelicsmod to BaseMod...");
        BaseMod.subscribe(this);
        logger.info(MOD_ID + " successfully subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        logger.info("Post-initialization started...");
        logger.info("Post-initialization complete.");
    }

    @Override
    public void receivePostDungeonInitialize() {
        logger.info("Post-dungeon initialization started...");
        seenRelics.clear();
        refreshingPools.clear(); // Clear refreshing state at the start of a new dungeon

        // Capture the relic pools at the start of the run
        commonRelics = new ArrayList<>(AbstractDungeon.commonRelicPool);
        uncommonRelics = new ArrayList<>(AbstractDungeon.uncommonRelicPool);
        rareRelics = new ArrayList<>(AbstractDungeon.rareRelicPool);
        shopRelics = new ArrayList<>(AbstractDungeon.shopRelicPool);
        bossRelics = new ArrayList<>(AbstractDungeon.bossRelicPool);

        logger.info("Captured relic pools at the start of the run.");
    }

    @Override
    public void receivePostUpdate() {
        // Ensure the logic only runs during an active dungeon run
        if (!AbstractDungeon.isPlayerInDungeon()) {
            return;
        }

        logger.debug("Post-update check for individual relic pools...");

        // Check and refresh each pool only if it is not currently being refreshed
        if (allRelicsSeenInPool(AbstractDungeon.commonRelicPool)) {
            logger.info("All common relics have been seen. Refreshing common relic pool...");
            refreshRelicPool(AbstractDungeon.commonRelicPool, "common");
        }
        if (allRelicsSeenInPool(AbstractDungeon.uncommonRelicPool)) {
            logger.info("All uncommon relics have been seen. Refreshing uncommon relic pool...");
            refreshRelicPool(AbstractDungeon.uncommonRelicPool, "uncommon");
        }
        if (allRelicsSeenInPool(AbstractDungeon.rareRelicPool)) {
            logger.info("All rare relics have been seen. Refreshing rare relic pool...");
            refreshRelicPool(AbstractDungeon.rareRelicPool, "rare");
        }
        if (allRelicsSeenInPool(AbstractDungeon.shopRelicPool)) {
            logger.info("All shop relics have been seen. Refreshing shop relic pool...");
            refreshRelicPool(AbstractDungeon.shopRelicPool, "shop");
        }
        if (allRelicsSeenInPool(AbstractDungeon.bossRelicPool)) {
            logger.info("All boss relics have been seen. Refreshing boss relic pool...");
            refreshRelicPool(AbstractDungeon.bossRelicPool, "boss");
        }
    }

    @Override
    public void receiveEditStrings() {
        logger.info("Loading localization strings...");
        String relicStringsPath = getLocalizationPath("RelicStrings.json");
        String uiStringsPath = getLocalizationPath("UIStrings.json");

        BaseMod.loadCustomStringsFile(RelicStrings.class, relicStringsPath);
        BaseMod.loadCustomStringsFile(UIStrings.class, uiStringsPath);

        logger.info("Localization files loaded successfully.");
    }

    @Override
    public void receiveEditKeywords() {
        logger.info("Loading keywords...");
        String keywordPath = getLocalizationPath("KeywordStrings.json");
        Gson gson = new Gson();
        FileHandle file = Gdx.files.internal(keywordPath);

        if (!file.exists()) {
            logger.error("Keyword file not found: " + keywordPath);
            return;
        }

        String json = file.readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);

        for (KeywordInfo keyword : keywords) {
            keyword.prep();
            BaseMod.addKeyword(MOD_ID, keyword.NAMES, keyword.DESCRIPTION);
            logger.debug("Loaded keyword: " + keyword.ID);
        }
        logger.info("Keywords loaded successfully.");
    }

    public static boolean allRelicsSeenInPool(List<String> relicPool) {
        if (relicPool == null) {
            logger.warn("Relic pool is null.");
            return true;
        }
        for (String relicID : relicPool) {
            if (!seenRelics.contains(relicID)) {
                logger.debug("Relic not seen: " + relicID);
                return false;
            }
        }
        return true;
    }

    public static void refreshRelicPool(List<String> relicPool, String poolName) {
        if (relicPool == null) {
            logger.warn(poolName + " relic pool is null. Skipping refresh.");
            return;
        }
        if (refreshingPools.contains(poolName)) {
            logger.debug(poolName + " relic pool is already being refreshed. Skipping.");
            return;
        }

        refreshingPools.add(poolName); // Mark the pool as being refreshed

        // Use the captured relic pools to repopulate the pool
        List<String> allRelics = getAllRelicsForPool(poolName);
        if (allRelics == null || allRelics.isEmpty()) {
            logger.error("No relics available to refresh " + poolName + " relic pool.");
            refreshingPools.remove(poolName); // Remove the pool from refreshing state
            return;
        }

        relicPool.clear();
        logger.info("Cleared " + poolName + " relic pool.");

        for (String relicID : allRelics) {
            relicPool.add(relicID);
            logger.debug("Re-added relic to " + poolName + " pool: " + relicID);
        }

        refreshingPools.remove(poolName); // Remove the pool from refreshing state
        logger.info(poolName + " relic pool has been refreshed.");
    }

    private static List<String> getAllRelicsForPool(String poolName) {
        // Use the captured relic pools
        switch (poolName.toLowerCase()) {
            case "common":
                return commonRelics;
            case "uncommon":
                return uncommonRelics;
            case "rare":
                return rareRelics;
            case "shop":
                return shopRelics;
            case "boss":
                return bossRelics;
            default:
                logger.error("Unknown relic pool: " + poolName);
                return null;
        }
    }

    private String getLocalizationPath(String fileName) {
        String lang = Settings.language.name().toLowerCase();
        String path = MOD_ID + "/localization/" + lang + "/" + fileName;
        logger.debug("Localization path: " + path);
        return path;
    }
}

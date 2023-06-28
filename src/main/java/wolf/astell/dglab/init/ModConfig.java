/*
Licenced under the [Choco Licence] https://emowolf.fun/choco
So let's build something awesome from this!
Author: Kels_Astell
GitHub: https://github.com/KelsAstell
*/
package wolf.astell.dglab.init;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wolf.astell.dglab.Main;

@Config(modid = Main.MODID, category = "")
public class ModConfig {
    @Mod.EventBusSubscriber(modid = Main.MODID)
    public static class ConfigSyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Main.MODID)) {
                ConfigManager.sync(Main.MODID, Config.Type.INSTANCE);
            }
        }
    }

    @Config.LangKey("dglab.conf")
    public static final DGConfig DG_CONFIG = new DGConfig();

    public static class DGConfig {
        @Config.LangKey("config.ws_port.name")
        @Config.RangeInt(min = 1, max = 65535)
        public int PORT = 23301;

        @Config.LangKey("config.punishtime.name")
        @Config.RangeInt(min = 1)
        public int PUNISH_TIME = 3;

        @Config.LangKey("config.punishrate.name")
        @Config.RangeInt(min = 1)
        public int PUNISH_RATE = 5;

        @Config.LangKey("config.maxA.name")
        @Config.RangeInt(min = 1)
        public int MAX_A = 50;

        @Config.LangKey("config.maxB.name")
        @Config.RangeInt(min = 1)
        public int MAX_B = 50;

        @Config.LangKey("config.baseA.name")
        @Config.RangeInt(min = 1)
        public int BASE_A = 10;

        @Config.LangKey("config.baseB.name")
        @Config.RangeInt(min = 1)
        public int BASE_B = 10;
    }
}


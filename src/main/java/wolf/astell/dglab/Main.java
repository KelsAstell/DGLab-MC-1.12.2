package wolf.astell.dglab;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Main.MODID, version = Main.VERSION, name = "DG-Lab for MC", acceptedMinecraftVersions = "[1.12.2]")
public class Main {
    public static final String MODID = "dglab";
    public static final String VERSION = "1.0.0";
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CommandDglabPunish());
        System.out.println("punish registered!");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        try {
            event.registerServerCommand(new CommandDglab());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

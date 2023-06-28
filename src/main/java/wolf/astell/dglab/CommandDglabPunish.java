package wolf.astell.dglab;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wolf.astell.dglab.init.ModConfig;

import java.util.Timer;
import java.util.TimerTask;

public class CommandDglabPunish extends CommandBase {

    private static boolean punishActive = false;
    private static boolean ultraPunishActive = false;
    private static int punishTime = ModConfig.DG_CONFIG.PUNISH_TIME; // 设置惩罚持续时间，单位为秒
    private static int punishRate = ModConfig.DG_CONFIG.PUNISH_RATE; // 设置惩罚比例
    private EntityPlayer playerToPunish;
    private long punishEndTime = 0;


    private Timer ultraPunishTimer;


    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();

            if (punishActive && System.currentTimeMillis() > punishEndTime) {
                float damage = event.getAmount();
                startPunish(player, damage);
            } else if (ultraPunishActive) {
                float damage = event.getAmount();
                startUltraPunish(player, damage);
            }
            float damage = event.getAmount();
        }
    }


    private void startPunish(final ICommandSender sender, float damage) {
        // 获取当前强度值
        CommandDglabStrength.queryStrength(sender, true);

        // 计算惩罚强度值
        int punishStrengthA = CommandDglabStrength.getCurrentStrengthA() + (int) (damage * punishRate);
        int punishStrengthB = CommandDglabStrength.getCurrentStrengthB() + (int) (damage * punishRate);


        // 设置新的强度值
        CommandDglabStrength.instance.setStrength(null, punishStrengthA, punishStrengthB);
        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.damage") + " §b" + damage + " §f"
                        + I18n.format("dglab.punish.strength") + " §e" + punishStrengthA + " / " + punishStrengthB));
        // 在惩罚结束时恢复基础强度值
        punishEndTime = System.currentTimeMillis() + (punishTime * 1000L);
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    CommandDglabStrength.instance.setStrength(sender, CommandDglabStrength.getBaseStrengthA(), CommandDglabStrength.getBaseStrengthB());
                }
            },
            punishTime * 1000L
        );
    }

    private void startUltraPunish(final ICommandSender sender, float damage) {
        // 获取当前强度值
        CommandDglabStrength.queryStrength(sender, true);
        // 计算惩罚强度值
        int punishStrengthA = (int) (damage * punishRate);
        int punishStrengthB = (int) (damage * punishRate);


        if (punishStrengthA + CommandDglabStrength.getCurrentStrengthA() > CommandDglabStrength.getMaxStrengthA()) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.exceed.1") + "A: " + CommandDglabStrength.getMaxStrengthA() + I18n.format("dglab.punish.exceed.2")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            punishStrengthA = CommandDglabStrength.getMaxStrengthA() - CommandDglabStrength.getCurrentStrengthA();
        }
        if (punishStrengthB + CommandDglabStrength.getCurrentStrengthB() > CommandDglabStrength.getMaxStrengthB()) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.exceed.1") + "B: " + CommandDglabStrength.getMaxStrengthB() + I18n.format("dglab.punish.exceed.2")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            punishStrengthB = CommandDglabStrength.getMaxStrengthB() - CommandDglabStrength.getCurrentStrengthB();
        }

        // 设置新的强度值
        // 我不知道你在写什么, 我也不知道这么改对不对
        int resultStrengthA = CommandDglabStrength.getCurrentStrengthA() + punishStrengthA;
        int resultStrengthB = CommandDglabStrength.getCurrentStrengthB() + punishStrengthB;
        CommandDglabStrength.instance.addStrength(sender, "a", resultStrengthA);
        CommandDglabStrength.instance.addStrength(sender, "b", resultStrengthB);


        //设置字体颜色
        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.ultra") + " " + I18n.format("dglab.punish.damage") + " §b" + damage + " §f"
                + I18n.format("dglab.punish.strength") + " §e" + punishStrengthA + " / " + punishStrengthB));


        // 在惩罚结束时恢复基础强度值
        punishEndTime = System.currentTimeMillis() + (punishTime * 1000L);
        if (ultraPunishTimer != null) {
            ultraPunishTimer.cancel(); // 取消上一个 Timer
        }
        ultraPunishTimer = new Timer();
        ultraPunishTimer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    CommandDglabStrength.instance.setStrength(sender, CommandDglabStrength.getBaseStrengthA(), CommandDglabStrength.getBaseStrengthB());
                }
            },
            punishTime * 1000L
        );
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/dglab <punish/ultraPunish> <start/stop>\n" +
                "/dglab setPunishTime [seconds]\n" +
                "/dglab setPunishRate [value]\n" +
                "/dglab getPunishSetting";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "punish": {
                if (sender instanceof EntityPlayer) {
                    playerToPunish = (EntityPlayer) sender;
                } else {
                    sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.player_only")).setStyle(new Style().setColor(TextFormatting.RED)));
                    return;
                }

                String option = args[1].toLowerCase();

                if (option.equals("start")) {
                    if (punishActive) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.error.started")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    } else {
                        if (ultraPunishActive) {
                            sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.error.running_ultra")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                            ultraPunishActive = false;
                        }
                        punishActive = true;
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.target_player") + playerToPunish.getName()).setStyle(new Style().setColor(TextFormatting.WHITE)));
                    }
                } else if (option.equals("stop")) {
                    if (!punishActive) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.error.no_task")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    } else {
                        punishActive = false;
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.target_player_stop") + playerToPunish.getName()).setStyle(new Style().setColor(TextFormatting.WHITE)));
                        playerToPunish = null;
                    }
                } else {
                    sender.sendMessage(new TextComponentString(getUsage(sender)).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                }
                break;
            }
            case "ultrapunish": {
                if (sender instanceof EntityPlayer) {
                    playerToPunish = (EntityPlayer) sender;
                } else {
                    sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.error.player_only")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    return;
                }

                String option = args[1].toLowerCase();

                if (option.equals("start")) {
                    if (ultraPunishActive) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.error.started_ultra")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    } else {
                        if (punishActive) {
                            sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.error.running")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                            punishActive = false;
                        }
                        ultraPunishActive = true;
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.ultrapunish.target_player") + playerToPunish.getName()).setStyle(new Style().setColor(TextFormatting.WHITE)));
                    }
                } else if (option.equals("stop")) {
                    if (!ultraPunishActive) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.ultrapunish.error.no_task")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    } else {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.ultrapunish.target_player_stop") + playerToPunish.getName()).setStyle(new Style().setColor(TextFormatting.WHITE)));
                        playerToPunish = null;
                        ultraPunishActive = false;
                    }
                }
                break;
            }
            case "setpunishtime":
                punishTime = ModConfig.DG_CONFIG.PUNISH_TIME = Integer.parseInt(args[1]);
                sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.settime") + punishTime));
                break;
            case "setpunishrate":
                punishRate = ModConfig.DG_CONFIG.PUNISH_RATE = Integer.parseInt(args[1]);
                sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.setrate") + punishRate));
                break;
            case "getpunishsetting":
                sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.info.1")  + " " + punishTime
                        + ", " + I18n.format("dglab.punish.info.2")  + " " + punishRate));
                break;
            default:
                sender.sendMessage(new TextComponentString(getUsage(sender)));
                break;
        }
    }
}

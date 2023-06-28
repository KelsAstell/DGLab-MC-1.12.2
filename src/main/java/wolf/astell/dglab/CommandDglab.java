package wolf.astell.dglab;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;

public class CommandDglab extends CommandBase {
    private final CommandDglabConnect connectHandler;
    private final CommandDglabStrength strengthHandler;
    private final CommandDglabPunish punishHandler;

    public CommandDglab() {
        connectHandler = new CommandDglabConnect();
        strengthHandler = new CommandDglabStrength();
        punishHandler = new CommandDglabPunish();
    }

    private void displayHelp(ICommandSender sender) {
        sender.sendMessage(new TextComponentTranslation("dglab.help.connect").setStyle(new Style().setColor(TextFormatting.YELLOW)));
    }

    @Override
    public String getName() {
        return I18n.format("dglab.cmd_name");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/dglab <command>\n" +
                "/dglab connect <your phone's ip>\n" +
                "/dglab disconnect\n" +
                "/dglab setStrength [0~maxStrengthA] [0~maxStrengthB]\n" +
                "/dglab getStrength\n" +
                "/dglab setMaxStrength [20~276] [20~276]\n" +
                "/dglab getMaxStrength\n" +
                "/dglab addStrength <A/B> [value]\n" +
                "/dglab setBaseStrength [value]\n" +
                "/dglab getBaseStrength\n" +
                "/dglab setPunishTime [seconds]\n" +
                "/dglab setPunishRate [value]\n" +
                "/dglab getPunishSetting\n";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            displayHelp(sender);
            return;
        }
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "connect":
                if (args.length < 2) {
                    throw new WrongUsageException(connectHandler.getUsage(sender));
                }
                connectHandler.execute(server, sender, Arrays.copyOfRange(args, 1, args.length));
                break;

            case "disconnect":
                connectHandler.execute(server, sender, args);
                break;

            case "getstrength":
            case "getbasestrength":
            case "getmaxstrength":
                strengthHandler.execute(server, sender, args);
                break;

            case "setmaxstrength":
            case "setbasestrength":
            case "addstrength":
            case "setstrength":
                if (args.length < 3) {
                    throw new WrongUsageException(strengthHandler.getUsage(sender));
                }
                strengthHandler.execute(server, sender, args);
                break;

            case "setpunishtime":
            case "setpunishrate":
                if (args.length < 2) {
                    throw new WrongUsageException(punishHandler.getUsage(sender));
                }
                punishHandler.execute(server, sender, args);
                break;

            case "getpunishsetting":
            case "punish":
            case "ultrapunish":
                punishHandler.execute(server, sender, args);
                break;

            default:
                System.out.println("2");
                throw new WrongUsageException(getUsage(sender));
        }
    }
}

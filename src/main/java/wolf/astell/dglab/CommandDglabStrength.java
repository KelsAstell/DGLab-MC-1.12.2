package wolf.astell.dglab;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import wolf.astell.dglab.init.ModConfig;

public class CommandDglabStrength extends CommandBase implements CommandDglabConnect.WebSocketMessageCallback {
    public static int maxStrengthA = ModConfig.DG_CONFIG.MAX_A;
    public static int maxStrengthB = ModConfig.DG_CONFIG.MAX_B;
    public static int currentStrengthA = 10;
    public static int currentStrengthB = 10;

    public static int getMaxStrengthA() {
        return maxStrengthA;
    }

    public static int getMaxStrengthB() {
        return maxStrengthB;
    }

    public static int getCurrentStrengthA() {
        return currentStrengthA;
    }

    public static int getCurrentStrengthB() {
        return currentStrengthB;
    }

    public static int getBaseStrengthA() {
        return baseStrengthA;
    }

    public static int getBaseStrengthB() {
        return baseStrengthB;
    }

    public static int baseStrengthA = ModConfig.DG_CONFIG.BASE_A;
    public static int baseStrengthB = ModConfig.DG_CONFIG.BASE_B;
    public static CommandDglabStrength instance = new CommandDglabStrength();

    @Override
    public void onWebSocketMessage(ICommandSender sender, String message) {
        JsonParser jsonParser = new JsonParser();
        JsonObject response = jsonParser.parse(message).getAsJsonObject();

        System.out.println(message);
        int responseId = response.get("id").getAsInt();

        switch (responseId) {
            case 100001: { // queryStrength response
                // Process the response
                JsonObject responseData = response.get("data").getAsJsonObject();
                int totalStrengthA = responseData.get("totalStrengthA").getAsInt();
                totalStrengthA = totalStrengthA == 0 ? 0 : totalStrengthA - 9;
                int totalStrengthB = responseData.get("totalStrengthB").getAsInt();
                totalStrengthB = totalStrengthB == 0 ? 0 : totalStrengthB - 9;

                // Send the message to the player
                sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.getcurrent") + " "
                        + totalStrengthA + " / " + totalStrengthB).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                break;
            }
            case 100002: {// setStrength response
                if (response.get("code").getAsInt() == 0) {
                    break;
                } else {
                    sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.seterror") + " " + response.get("result").getAsString()).setStyle(new Style().setColor(TextFormatting.RED )));
                    System.out.println("Failed to set strength values.");
                }
                break;
            }
            case 100003: {  //addStrength response
                if (response.get("code").getAsInt() == 0) {
                    break;
                } else {
                    System.out.println("Failed to add strength values.");
                    sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.adderror") + " " + response.get("result").getAsString()).setStyle(new Style().setColor(TextFormatting.RED )));
                }
                break;
            }
            case 100000: {
                break;
            }
        }
    }

    public void setStrength(ICommandSender sender, int strengthA, int strengthB) {
        if (strengthA > maxStrengthA) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.exceed.1") + "A: " + maxStrengthA + I18n.format("dglab.punish.exceed.2")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            strengthA = maxStrengthA;
        }
        if (strengthB > maxStrengthB) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.exceed.1") + "B: " + maxStrengthB + I18n.format("dglab.punish.exceed.2")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            strengthB = maxStrengthB;
        }

        // 获取已建立连接的 WebSocketClient
        WebSocket client = CommandDglabConnect.getClientInstance();

        if (client == null || !client.isOpen()) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.no_ws")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            return;
        }
        currentStrengthA = strengthA;
        currentStrengthB = strengthB;
        strengthA = strengthA == 0 ? 0 : strengthA + 9;
        strengthB = strengthB == 0 ? 0 : strengthB + 9;


        // 创建 JSON 请求
        JsonObject requestData = new JsonObject();
        requestData.addProperty("strengthA", strengthA);
        requestData.addProperty("strengthB", strengthB);

        JsonObject request = new JsonObject();
        request.addProperty("id", 100002);
        request.addProperty("method", "setStrength");
        request.add("data", requestData);

        // 发送 JSON 请求
        client.sendText(request.toString());
    }

    public void addStrength(ICommandSender sender, String channel, int addedStrength) {
        queryStrength(sender, true);
        boolean channelA;
        if (channel.equals("a")) {
            channelA = true;
            if (currentStrengthA + addedStrength > maxStrengthA) {
                addedStrength = maxStrengthA - currentStrengthA;
                sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.exceed.1") + "A: " + maxStrengthA + I18n.format("dglab.punish.exceed.2")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            }
            // 更新 currentStrengthA
            currentStrengthA = currentStrengthA + addedStrength;
        } else if (channel.equals("b")) {
            channelA = false;
            if (currentStrengthB + addedStrength > maxStrengthB) {
                addedStrength = maxStrengthB - currentStrengthB;
                sender.sendMessage(new TextComponentString(I18n.format("dglab.punish.exceed.1") + "B: " + maxStrengthB + I18n.format("dglab.punish.exceed.2")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            }
            // 更新 currentStrengthB
            currentStrengthB = currentStrengthB + addedStrength;
        } else {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.invalid_channel")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            return;
        }

        // 获取已建立连接的 WebSocketClient
        WebSocket client = CommandDglabConnect.getClientInstance();

        if (client == null || !client.isOpen()) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.no_ws")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            return;
        }

        // 创建 JSON 请求
        JsonObject requestData = new JsonObject();
        requestData.addProperty("channel", channelA);
        requestData.addProperty("strength", addedStrength);

        JsonObject request = new JsonObject();
        request.addProperty("id", 100003); // 使用唯一ID
        request.addProperty("method", "addStrength");
        request.add("data", requestData);

        // 发送 JSON 请求
        client.sendText(request.toString());
        System.out.println(request);
    }

    public static void queryStrength(ICommandSender sender, boolean silent) {
        // 获取已建立连接的 WebSocketClient
        WebSocket client = CommandDglabConnect.getClientInstance();

        if (client == null || !client.isOpen()) {
            if (!silent) {
                sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.no_ws")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            }
            return;
        }

        // 创建 JSON 请求
        JsonObject request = new JsonObject();
        if (!silent) {
            request.addProperty("id", 100001);
        } else {
            request.addProperty("id", 100000);
        }

        request.addProperty("method", "queryStrength");

        // 发送 JSON 请求
        client.sendText(request.toString());
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String subCommand = args[0].toLowerCase();
        WebSocket client = CommandDglabConnect.getClientInstance();

        if (client == null || !client.isOpen()) {
            sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.no_ws")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
            return;
        }
        // Set the callback for the WebSocket messages
        CommandDglabConnect.setWebSocketMessageCallback(this);

        switch (subCommand) {
            case "getstrength":
                queryStrength(sender, false);
                break;
            case "setstrength":
                int strengthA = Integer.parseInt(args[1]);
                int strengthB = Integer.parseInt(args[2]);
                setStrength(sender, strengthA, strengthB);
                break;
            case "addstrength":
                if (args.length < 3) {
                    sender.sendMessage(new TextComponentString(I18n.format("dglab.help.strength")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    return;
                }
                String channel = args[1].toLowerCase();
                int strength = Integer.parseInt(args[2]);
                addStrength(sender, channel, strength);
                break;
            case "setbasestrength":
                baseStrengthA = ModConfig.DG_CONFIG.BASE_A = Integer.parseInt(args[1]);
                baseStrengthB = ModConfig.DG_CONFIG.BASE_B = Integer.parseInt(args[2]);
                sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.setbase") + " "
                        + baseStrengthA + " / " + baseStrengthB).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                break;
            case "getbasestrength":
                sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.getbase") + " "
                        + baseStrengthA + " / " + baseStrengthB).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                break;
            case "setmaxstrength":
                maxStrengthA = ModConfig.DG_CONFIG.MAX_A = Integer.parseInt(args[1]);
                maxStrengthB = ModConfig.DG_CONFIG.MAX_B = Integer.parseInt(args[2]);
                sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.setmax") + " "
                        + maxStrengthA + " / " + maxStrengthB).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                break;
            case "getmaxstrength":
                sender.sendMessage(new TextComponentString(I18n.format("dglab.strength.getmax") + " "
                        + maxStrengthA + " / " + maxStrengthB).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                break;
        }
    }
}

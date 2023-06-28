package wolf.astell.dglab;

import com.neovisionaries.ws.client.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import wolf.astell.dglab.init.ModConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class CommandDglabConnect extends CommandBase {
    private static WebSocket ws;
    private static CommandDglabConnect instance;

    public CommandDglabConnect() {
        instance = this;
    }

    public static WebSocket getClientInstance() {
        if (instance != null && ws != null) {
            System.out.println(I18n.format("dglab.ws.found") + " " + ws);
        } else {
            System.out.println(I18n.format("dglab.ws.not_found"));
        }
        return ws;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return I18n.format("dglab.help.connect");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        String subCommand = args[0].toLowerCase();
        if (!subCommand.equals("disconnect")) {
            final String deviceAddress = args[0];
            int wsPort = ModConfig.DG_CONFIG.PORT;

            try {
                URI deviceUri = new URI("ws://" + deviceAddress + ":" + wsPort);

                WebSocketFactory factory = new WebSocketFactory();
                ws = factory.createSocket(deviceUri);

                ws.addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket websocket, String message) {
                        if (messageCallback != null) {
                            messageCallback.onWebSocketMessage(sender, message);
                        }
                    }

                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.ws.connected") + " " + deviceAddress).setStyle(new Style().setColor(TextFormatting.WHITE)));
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.ws.disconnected") + " " + deviceAddress).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) {
                        sender.sendMessage(new TextComponentString(I18n.format("dglab.ws.error") + " " + cause.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
                    }
                });

                ws.connect();

            } catch (URISyntaxException e) {
                sender.sendMessage(new TextComponentString(I18n.format("dglab.ws.error") + " " + e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
                e.printStackTrace();
            } catch (IOException | WebSocketException e) {
                e.printStackTrace();
            }
        } else {
            ws.disconnect();
            sender.sendMessage(new TextComponentString(I18n.format("dglab.ws.terminated")).setStyle(new Style().setColor(TextFormatting.YELLOW)));
        }
    }

    public interface WebSocketMessageCallback {
        void onWebSocketMessage(ICommandSender sender, String message);
    }

    private static WebSocketMessageCallback messageCallback;

    public static void setWebSocketMessageCallback(WebSocketMessageCallback callback) {
        messageCallback = callback;
    }
}

package fun.zorvex.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import java.util.Locale;

public final class ZorvexPowersClient implements ClientModInitializer {
    private static volatile String currentPower;

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.clientboundPlay().register(PowerSyncPayload.TYPE, PowerSyncPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PowerSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> setPower(payload.power())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> setPower(null));
        PowerHud.register();
    }

    static String getPower() { return currentPower; }

    private static void setPower(String rawPower) {
        if (rawPower == null || rawPower.isBlank() || rawPower.equalsIgnoreCase("NONE")) {
            currentPower = null;
            return;
        }
        String normalized = rawPower.trim().toUpperCase(Locale.ROOT);
        currentPower = switch (normalized) {
            case "AGNI", "BOREAS", "OCEANIC", "EOS", "YMIR", "CORROSIVE", "ASTRAEUS", "CHAINED" -> normalized;
            default -> null;
        };
    }
}

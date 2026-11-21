package fun.zorvex.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PowerHud {
    private PowerHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("zorvexpowers", "power_hud"),
                PowerHud::render
        );
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        String power = ZorvexPowersClient.getPower();
        if (client.player == null || client.gui.hud.isHidden() || power == null) return;

        int accent = colorFor(power);
        graphics.fill(8, 8, 174, 39, 0xB0101018);
        graphics.fill(8, 8, 11, 39, accent);
        graphics.text(client.font, Component.literal("ZORVEX POWERS"), 18, 13, 0xFFFFFFFF, true);
        graphics.text(client.font, Component.literal(power), 18, 25, accent, true);
    }

    private static int colorFor(String power) {
        return switch (power) {
            case "AGNI" -> 0xFFFF5555;
            case "BOREAS" -> 0xFF55FFFF;
            case "OCEANIC" -> 0xFF00AAAA;
            case "EOS" -> 0xFFFFFF55;
            case "YMIR" -> 0xFF5555FF;
            case "CORROSIVE" -> 0xFF55AA00;
            case "ASTRAEUS" -> 0xFFFF55FF;
            case "CHAINED" -> 0xFFAAAAAA;
            default -> 0xFFFFFFFF;
        };
    }
}

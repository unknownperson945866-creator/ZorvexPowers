package fun.zorvex.powers;

import org.bukkit.ChatColor;

public enum PowerType {
    AGNI("Agni", ChatColor.RED, "Master the heat of fire."),
    BOREAS("Boreas", ChatColor.AQUA, "Command the freezing wind."),
    OCEANIC("Oceanic", ChatColor.DARK_AQUA, "Harness the force of water."),
    EOS("Eos", ChatColor.YELLOW, "Channel radiant energy."),
    YMIR("Ymir", ChatColor.BLUE, "Wield ancient frost."),
    CORROSIVE("Corrosive", ChatColor.DARK_GREEN, "Control corrosive nature."),
    ASTRAEUS("Astraeus", ChatColor.LIGHT_PURPLE, "Draw power from the cosmos."),
    CHAINED("Chained", ChatColor.DARK_GRAY, "Bend shadow and binding energy.");

    private final String displayName;
    private final ChatColor color;
    private final String description;

    PowerType(String displayName, ChatColor color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public String displayName() { return displayName; }
    public ChatColor color() { return color; }
    public String description() { return description; }

    public static PowerType fromString(String input) {
        try {
            return valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }
}

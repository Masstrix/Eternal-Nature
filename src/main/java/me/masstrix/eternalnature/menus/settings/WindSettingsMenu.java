package me.masstrix.eternalnature.menus.settings;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigPath;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.menus.Button;
import me.masstrix.eternalnature.menus.GlobalMenu;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.eternalnature.util.ChangeToggleUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

@Configurable.Path("global.wind")
public class WindSettingsMenu extends GlobalMenu {

    private final EternalNature PLUGIN;
    private final MenuManager MANAGER;
    private final LanguageEngine LANG;
    private final Configuration CONFIG;

    private boolean enabled;
    private boolean gustsEnabled;
    private double gustChance;
    private double gustStrength;
    private boolean showParticles;
    private boolean pushPlayers;
    private boolean pushAnimals;

    public WindSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.LEAF_PARTICLE_SETTINGS, 5);
        this.PLUGIN = plugin;
        this.MANAGER = menuManager;
        this.LANG = plugin.getLanguageEngine();
        this.CONFIG = plugin.getRootConfig();
    }

    @Override
    public String getTitle() {
        return LANG.getText("menu.wind.title");
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        gustsEnabled = section.getBoolean("gusts.enabled");
        gustChance = section.getDouble("gusts.chance");
        gustChance = section.getDouble("gusts.max-strength");
        gustsEnabled = section.getBoolean("gusts.push-players");
        gustsEnabled = section.getBoolean("gusts.push-entities");
        gustsEnabled = section.getBoolean("gusts.push-particles");
        build();
    }

    @Override
    public void build() {
        addBackButton(MANAGER, Menus.SETTINGS);

        setButton(new Button(asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName(LANG.getText("menu.wind.enabled.title"))
                .addDescription(LANG.getText("menu.wind.enabled.description"))
                .addSwitch("Currently:", enabled)
                .build()).setToggle(LANG.getText("menu.wind.enabled.title"), () -> enabled)
                .onClick(player -> {
                    CONFIG.set(ConfigPath.LEAF_EFFECT_ENABLED, (enabled = !enabled));
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 4), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName(LANG.getText("menu.wind.gusts.enabled.title"))
                .addDescription(LANG.getText("menu.wind.gusts.enabled.description"))
                .addSwitch("Currently:", gustsEnabled)
                .build()).setToggle(LANG.getText("menu.wind.gusts.enabled.title"), () -> gustsEnabled)
                .onClick(player -> {
                    CONFIG.set("global.wind.gusts.enabled", (gustsEnabled = !gustsEnabled));
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        ChangeToggleUtil maxSelector = new ChangeToggleUtil();
        maxSelector.add("&b" + Button.Common.CUSTOM.text(LANG), 15.001);
        maxSelector.add("&c" + Button.Common.EXTREME.text(LANG), 15);
        maxSelector.add("&6" + Button.Common.HIGH.text(LANG), 8);
        maxSelector.add("&e" + Button.Common.MEDIUM.text(LANG), 5);
        maxSelector.add("&a" + Button.Common.LOW.text(LANG), 2);
        maxSelector.selectClosest(gustChance);

        setButton(new Button(asSlot(1, 6), () -> new ItemBuilder(Material.RABBIT_HIDE)
                .setName(LANG.getText("menu.wind."))
                .addDescription(LANG.getText(""))
                .addAction(LANG.getText(""))
                .build()));

        setButton(new Button(asSlot(1, 6), () -> new ItemBuilder(Material.RABBIT_HIDE)
                .setName(LANG.getText("menu.wind."))
                .addDescription(LANG.getText(""))
                .addAction(LANG.getText(""))
                .build())
                .setToggle("menu.wind.push-players", () -> pushPlayers)
                .onClick(p -> {
                    // TODO
                }));
    }
}
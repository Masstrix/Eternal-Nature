package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

public class SettingsGUI implements Listener {

    private int hydrationEnabled = asSlot(1, 1);
    private int hydrationDisplay = asSlot(2, 7);
    private int temperatureDisplay = asSlot(1, 7);
    private int hydrationDamage = asSlot(1, 4);
    private int temperatureDamage = asSlot(1, 4);
    private int waterfalls = asSlot(1, 2);
    private int autoplant = asSlot(1, 3);
    private int rebuildConfig = asSlot(1, 8);

    private EternalNature plugin;
    private Inventory gui;

    public SettingsGUI(EternalNature plugin) {
        this.plugin = plugin;
        gui = Bukkit.createInventory(null, 9 * 4, "Eternal Nature Settings");
        updateItems();

        new BukkitRunnable() {
            @Override
            public void run() {

                World world = Bukkit.getWorlds().get(0);
                long time = world.getFullTime();
                long days =  time / 24000L;

                int daysInYear = 40;

                int season = (int) ((days % daysInYear) / 4);
                String seasonName = "Spring";

                int split = daysInYear / 4;

                if (season > split * 3) {
                    seasonName = "Winter";
                }
                else if (season > split * 2) {
                    seasonName = "Autumn";
                }
                else if (season > split) {
                    seasonName = "Summer";
                }

                gui.setItem(0, new ItemBuilder(Material.CLOCK)
                        .setName("&e&lTesting")
                        .addLore("", "Time: &f" + world.getTime(), "Season: &f" + seasonName, "Day: &f" + days)
                        .build());
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    public void updateItems() {
        SystemConfig config = plugin.getSystemConfig();
        gui.setItem(hydrationEnabled, new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.WATER)
                .setName("&aHydration Enabled")
                .addLore("")
                .addLore("Toggle if hydration is")
                .addLore("enabled.")
                .addLore("")
                .addSwitch("Currently:", config.isHydrationEnabled())
                .build());
        setToggle("Hydration", hydrationEnabled + 9, config.isHydrationEnabled());

        gui.setItem(hydrationDisplay, new ItemBuilder(Material.JUNGLE_SIGN)
                .setName("&aHydration Display")
                .addLore("")
                .addLore("Toggle the display mode")
                .addLore("for players hydration bar.")
                .addLore("")
                .addLore("Currently: &f" + config.getThirstRenderMethod())
                .addLore("&eClick to change to " + config.getThirstRenderMethod().opisite())
                .build());

        gui.setItem(temperatureDisplay, new ItemBuilder(Material.ACACIA_SIGN)
                .setName("&aTemperature Display")
                .addLore("")
                .addLore("Toggle the display mode")
                .addLore("for players hydration bar.")
                .addLore("")
                .addLore("Currently: &f" + config.getTempRenderMethod())
                .addLore("&eClick to change to " + config.getTempRenderMethod().opisite())
                .build());
        gui.setItem(waterfalls, new ItemBuilder(Material.TUBE_CORAL)
                .setName("&aWaterfalls")
                .addLore("")
                .addLore("Toggle if waterfalls are")
                .addLore("visible.")
                .addLore("")
                .addSwitch("Currently:", config.areWaterfallsEnabled())
                .build());
        setToggle("Waterfalls", waterfalls + 9, config.areWaterfallsEnabled());

        gui.setItem(autoplant, new ItemBuilder(Material.OAK_SAPLING)
                .setName("&aAuto Plant")
                .addLore("")
                .addLore("Toggle if saplings are")
                .addLore("automatically planted if left")
                .addLore("long enough.")
                .addLore("")
                .addSwitch("Currently:", config.isAutoPlantSaplings())
                .build());
        setToggle("Auto Plant", autoplant + 9, config.isAutoPlantSaplings());

        gui.setItem(hydrationDamage, new ItemBuilder(Material.TUBE_CORAL)
                .setName("&aHydration damage")
                .addLore("")
                .addLore("If players have no H²O left")
                .addLore("damage will slowly be caused.")
                .addLore("")
                .addSwitch("Currently:", config.isHydrationCauseDamage())
                .build());
        setToggle("Hydration Damage", hydrationDamage + 9, config.isHydrationCauseDamage());

        gui.setItem(rebuildConfig, new ItemBuilder(Material.SCAFFOLDING)
                .setName("&aRebuild Config")
                .addLore("")
                .addLore("If your config file has become")
                .addLore("messy or might contain invalid")
                .addLore("this will clean it up.")
                .addLore("")
                .addLore("&eClick to fix config file.")
                .build());
    }

    private void setToggle(String name, int slot, boolean enable) {
        gui.setItem(slot, new ItemBuilder(enable ? Material.LIME_STAINED_GLASS_PANE
                : Material.GRAY_STAINED_GLASS_PANE)
                .setName(enable ? "&7" + name + ":&a Enabled" : "&7" + name + ":&c Disabled").build());

    }

    private int asSlot(int row, int column) {
        return (row * 9) + column;
    }

    public void open(Player player) {
        player.openInventory(gui);
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null || !inv.equals(gui)) return;
        int slot = event.getSlot();
        event.setCancelled(true);

        SystemConfig config = plugin.getSystemConfig();
        Player player = (Player) event.getWhoClicked();

        if (slot == rebuildConfig) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.closeInventory();
            config.rebuildConfig();
            player.sendMessage(StringUtil.color(PluginData.PREFIX + "Config has been fixed."));
        }
        else if (slot == hydrationDisplay) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            config.setThirstRenderMethod(config.getThirstRenderMethod().opisite());
            config.save();
            updateItems();
        }
        else if (slot == temperatureDisplay) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            config.setTempRenderMethod(config.getTempRenderMethod().opisite());
            config.save();
            updateItems();
        }
        else if (slot == waterfalls) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            config.setWaterfallsEnabled(!config.areWaterfallsEnabled());
            config.save();
            updateItems();
        }
        else if (slot == hydrationDamage) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            config.setHydrationCauseDamage(!config.isHydrationCauseDamage());
            config.save();
            updateItems();
        }
        else if (slot == autoplant) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            config.setAutoPlantSaplings(!config.isAutoPlantSaplings());
            config.save();
            updateItems();
        }
    }
}

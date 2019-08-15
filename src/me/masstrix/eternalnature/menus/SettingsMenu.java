package me.masstrix.eternalnature.menus;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenu implements Listener {

    private List<Button> buttons = new ArrayList<>();

    private EternalNature plugin;
    private Inventory main, hydration, temp, other;

    public SettingsMenu(EternalNature plugin) {
        this.plugin = plugin;
        main = Bukkit.createInventory(null, 9 * 5, "Eternal Nature Settings");
        hydration = Bukkit.createInventory(null, 9 * 5, "Hydration Settings");
        temp = Bukkit.createInventory(null, 9 * 5, "Temperature Settings");
        other = Bukkit.createInventory(null, 9 * 5, "Other Settings");
        SystemConfig config = plugin.getSystemConfig();

        ItemStack backIcon = new ItemBuilder(Material.ARROW).setName("&aGo Back").build();

        // Main Menu buttons
        buttons.add(new Button(main, asSlot(1, 3), () -> new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.JUMP)
                .setName("&aHydration Settings")
                .addLore("", "Change the settings for how", "hydration works.", "")
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .addLore("", "&eClick to view & edit")
                .build()).onClick(player -> {
            player.openInventory(hydration);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        }));
        buttons.add(new Button(main, asSlot(1, 5), () -> new ItemBuilder(Material.CAMPFIRE)
                .setName("&aTemperature Settings")
                .addLore("", "Change the settings for how", "temperature works.", "")
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.TEMP_ENABLED))
                .addLore("", "&eClick to view & edit")
                .build()).onClick(player -> {
            player.openInventory(temp);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        }));
        buttons.add(new Button(main, asSlot(3, 2), () -> new ItemBuilder(Material.WATER_BUCKET)
                .setName("&aWaterfalls")
                .addLore("", "Set if waterfalls are enabled.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.WATERFALLS))
                .build()).setToggle("Waterfalls", () -> config.isEnabled(ConfigOption.WATERFALLS))
                .onClick(player -> {
                    config.toggle(ConfigOption.WATERFALLS);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
        buttons.add(new Button(main, asSlot(3, 3), () -> new ItemBuilder(Material.KELP)
                .setName("&aFalling Leaves")
                .addLore("", "Set if leave blocks will emmit", "a leaf particle randomly.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.LEAF_EFFECT))
                .build()).setToggle("Falling Leaves", () -> config.isEnabled(ConfigOption.LEAF_EFFECT))
                .onClick(player -> {
                    config.toggle(ConfigOption.LEAF_EFFECT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
        buttons.add(new Button(main, asSlot(3, 4), () -> new ItemBuilder(Material.OAK_SAPLING)
                .setName("&aAuto Plant Saplings")
                .addLore("", "Set if saplings have a chance to", "auto plant them self.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.LEAF_EFFECT))
                .build()).setToggle("Auto Plant Saplings", () -> config.isEnabled(ConfigOption.AUTO_PLANT_SAPLING))
                .onClick(player -> {
                    config.toggle(ConfigOption.AUTO_PLANT_SAPLING);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
        buttons.add(new Button(main, asSlot(3, 6), () -> new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("&aUpdate Checks")
                .addLore("", "Toggle if automatic checks for updates", "are done.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.UPDATES_CHECK))
                .build()).setToggle("Update Checks", () -> config.isEnabled(ConfigOption.UPDATES_CHECK))
                .onClick(player -> {
                    config.toggle(ConfigOption.UPDATES_CHECK);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
        buttons.add(new Button(main, asSlot(3, 7), () -> new ItemBuilder(Material.NAME_TAG)
                .setName("&aUpdate Notifications")
                .addLore("", "Toggle if you are notified of updates.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.UPDATES_NOTIFY))
                .build()).setToggle("Update Notifications", () -> config.isEnabled(ConfigOption.UPDATES_NOTIFY))
                .onClick(player -> {
                    config.toggle(ConfigOption.UPDATES_NOTIFY);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Hydration Menu buttons
        buttons.add(new Button(hydration, 0, backIcon).onClick(player -> {
            player.openInventory(main);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        }));
        buttons.add(new Button(hydration, asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&aHydration Enabled")
                .addLore("", "Set if hydration is enabled", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .build()).setToggle("Enabled", () -> config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .onClick(player -> {
            config.toggle(ConfigOption.HYDRATION_ENABLED);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
        buttons.add(new Button(hydration, asSlot(1, 4), () -> new ItemBuilder(Material.IRON_SWORD)
                .setName("&aHydration Damage")
                .addLore("", "Set if players will be hurt if", "there hydration is empty.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.HYDRATION_DAMAGE))
                .build()).setToggle("Cause Damage", () -> config.isEnabled(ConfigOption.HYDRATION_DAMAGE))
                .onClick(player -> {
            config.toggle(ConfigOption.HYDRATION_DAMAGE);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
        buttons.add(new Button(hydration, asSlot(1, 5), () -> new ItemBuilder(Material.RABBIT_FOOT)
                .setName("&aHydration Walking")
                .addLore("", "Set if hydration will be lost from", "waling and other movements.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.HYDRATION_WALKING))
                .build()).setToggle("Walking", () -> config.isEnabled(ConfigOption.HYDRATION_WALKING))
                .onClick(player -> {
            config.toggle(ConfigOption.HYDRATION_WALKING);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));

        // Temperature Menu buttons
        buttons.add(new Button(temp, 0, backIcon).onClick(player -> {
            player.openInventory(main);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        }));
        buttons.add(new Button(temp, asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&aTemperature Enabled")
                .addLore("", "Set if temperature is enabled", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMP_ENABLED))
                .build()).setToggle("Enabled", () -> config.isEnabled(ConfigOption.TEMP_ENABLED))
                .onClick(player -> {
            config.toggle(ConfigOption.TEMP_ENABLED);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
        buttons.add(new Button(temp, asSlot(1, 4), () -> new ItemBuilder(Material.IRON_SWORD)
                .setName("&aTemperature Damage")
                .addLore("", "Set if players will be hurt if", "there temperature is to high or low.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMP_DAMAGE))
                .build()).setToggle("Cause Damage", () -> config.isEnabled(ConfigOption.TEMP_DAMAGE))
                .onClick(player -> {
            config.toggle(ConfigOption.TEMP_DAMAGE);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
        buttons.add(new Button(temp, asSlot(1, 5), () -> new ItemBuilder(Material.POTION)
                .setName("&aSweating")
                .addLore("", "If enabled players will sweat and lose", "hydration faster in higher temperatures.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMP_SWEAT))
                .build()).setToggle("Sweat", () -> config.isEnabled(ConfigOption.TEMP_SWEAT))
                .onClick(player -> {
            config.toggle(ConfigOption.TEMP_SWEAT);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));

        update(); // Adds all the items to the inventories
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//
//                World world = Bukkit.getWorlds().get(0);
//                long time = world.getFullTime();
//                long days =  time / 24000L;
//
//                int daysInYear = 40;
//
//                int season = (int) ((days % daysInYear) / 4);
//                String seasonName = "Spring";
//
//                int split = daysInYear / 4;
//
//                if (season > split * 3) {
//                    seasonName = "Winter";
//                }
//                else if (season > split * 2) {
//                    seasonName = "Autumn";
//                }
//                else if (season > split) {
//                    seasonName = "Summer";
//                }
//
//                gui.setItem(0, new ItemBuilder(Material.CLOCK)
//                        .setName("&e&lTesting")
//                        .addLore("", "Time: &f" + world.getTime(), "Season: &f" + seasonName, "Day: &f" + days)
//                        .build());
//            }
//        }.runTaskTimer(plugin, 0, 10);
    }

    public void update() {
        buttons.forEach(Button::update);
    }

    private void setToggle(Inventory inv, String name, int slot, boolean enable) {
        main.setItem(slot, new ItemBuilder(enable ? Material.LIME_STAINED_GLASS_PANE
                : Material.GRAY_STAINED_GLASS_PANE)
                .setName(enable ? "&7" + name + ":&a Enabled" : "&7" + name + ":&c Disabled").build());
    }

    private int asSlot(int row, int column) {
        return (row * 9) + column;
    }

    public void open(Player player) {
        player.openInventory(main);
    }

    private boolean isValidMenu(Inventory inventory) {
        return inventory.equals(main) || inventory.equals(hydration) || inventory.equals(temp) || inventory.equals(other);
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null || !isValidMenu(inv)) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        buttons.forEach(b -> b.click(player, inv, slot));
    }
}

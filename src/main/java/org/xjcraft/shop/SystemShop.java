package org.xjcraft.shop;

import org.bukkit.command.PluginCommand;
import org.xjcraft.CommonPlugin;

public final class SystemShop extends CommonPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        loadConfigs();
        PluginCommand cmd = getCommand("cmd");
        SystemShopManager executor = new SystemShopManager(this);
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

package org.xjcraft.shop;

import com.zjyl1994.minecraftplugin.multicurrency.services.BankService;
import com.zjyl1994.minecraftplugin.multicurrency.services.CurrencyService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.CurrencyInfoEntity;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.xjcraft.shop.bean.Trade;
import org.xjcraft.shop.config.Config;
import org.xjcraft.utils.ScriptUtil;
import org.xjcraft.utils.StringUtil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SystemShopManager implements CommandExecutor, TabCompleter {
    private final SystemShop plugin;

    public SystemShopManager(SystemShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("这个指令必须由玩家执行！");
            return false;
        }
        Player player = (Player) commandSender;
        if (strings.length < 1) {
            player.sendMessage("可以使用的指令有：");
            for (Trade trade : Config.getIndex().values()) {
                sendHelp(trade, player);
            }
        } else {
            String subcmd = strings[0];
            Trade trade = Config.getIndex().get(subcmd);
            if (trade == null) {
                player.sendMessage("不存在的指令");
                return false;
            }
            if (strings.length != trade.getArgs().size() + 1) {
                player.sendMessage("指令长度错误！");
                sendHelp(trade, player);
                return false;
            }
            HashMap<String, String> placeholder = new HashMap<>();
            List<String> args = trade.getArgs();
            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);
                String value = strings[i + 1];
                try {
                    float v = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    player.sendMessage("不允许输入除了数字以外的值！");
                    sendHelp(trade, player);
                    return false;
                }
                placeholder.put(arg, value);
            }
            String parse = ScriptUtil.parse(placeholder, trade.getAmount());
            if (parse == null) {
                player.sendMessage("计算错误！请联系OP进行修复！");
                return false;
            }
            BigDecimal cost = new BigDecimal(parse);
            placeholder.put("x", String.format("%.1f", player.getLocation().getX()));
            placeholder.put("y", String.format("%.1f", player.getLocation().getY()));
            placeholder.put("z", String.format("%.1f", player.getLocation().getZ()));
            placeholder.put("world", player.getLocation().getWorld().getName());
            String cmd = StringUtil.applyPlaceHolder(placeholder, trade.getCmd());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                OperateResult info = CurrencyService.getCurrencyInfo(trade.getCurrency());
                if (!info.getSuccess()) {
                    player.sendMessage("配置错误！请联系OP进行修复！");
                    return;
                }
                CurrencyInfoEntity data = (CurrencyInfoEntity) info.getData();
                String owner = data.getOwner();
                if(cost.compareTo(BigDecimal.ZERO)<=0){
                    player.sendMessage("错误的数值！");
                    return;
                }
                OperateResult result = BankService.transferTo(player.getName(), owner, trade.getCurrency(), cost, TxTypeEnum.ELECTRONIC_TRANSFER_OUT, "指令商店" + trade.getId() + ":" + cost.toString());
                if (result.getSuccess()) {
                    player.sendMessage(String.format("消费成功！扣除了%.2f的%s,%s", cost, data.getName(),trade.getDesc()));
                    plugin.getLogger().info(String.format("%s spend %.2f %s to dispatch:%s", player.getName(), cost, trade.getCurrency(), cmd));
                    plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd));
                }

            });
        }
        return true;
    }

    private void sendHelp(Trade trade, Player player) {
        StringBuilder m = new StringBuilder(String.format("/cmd %s", trade.getId()));
        for (String arg : trade.getArgs()) {
            m.append(" <").append(arg).append(">");
        }
        m.append("  ").append(trade.getDesc());
        player.sendMessage(m.toString());
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] strings) {
        if (strings.length == 1) {
            return Config.getIndex().keySet().stream().filter(s -> s.startsWith(strings[0])).collect(Collectors.toList());
        } else {
            String sub = strings[0];
            Trade trade = Config.getIndex().get(sub);
            if (trade == null) {
                return null;
            }
            int index = strings.length - 1;
            if (index <= trade.getArgs().size()) {
                List<String> remain = trade.getArgs().subList(index-1, trade.getArgs().size());
//                String[] remain = Arrays.copyOfRange(trade.getArgs(), index, trade.getArgs().size());
                return Collections.singletonList(StringUtil.join(remain.toArray(), ""));
            }
        }
        return Collections.EMPTY_LIST;
    }
}

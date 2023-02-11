package org.xjcraft.shop.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.xjcraft.api.SimpleConfigurationSerializable;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
public class Trade implements SimpleConfigurationSerializable {
    String id;
    String cmd;
    String currency;
    String amount;
    String desc;
    List<String> args;

    public Trade(String id, String cmd, String currency, String amount, String desc, String... args) {
        this.id = id;
        this.cmd = cmd;
        this.currency = currency;
        this.amount = amount;
        this.desc = desc;
        this.args = Arrays.asList(args);
    }
}

package org.xjcraft.shop.config;

import lombok.Data;
import lombok.Getter;
import org.xjcraft.annotation.Ignore;
import org.xjcraft.annotation.Instance;
import org.xjcraft.annotation.RConfig;
import org.xjcraft.api.ConfigurationInitializable;
import org.xjcraft.shop.bean.Trade;

import java.util.*;
import java.util.stream.Collectors;

@RConfig
@Data
public class Config implements ConfigurationInitializable {
    @Instance
    public static Config config = new Config();
    List<Trade> trades = new ArrayList<Trade>() {{
        add(new Trade("render", "bluemap force-update %world% %x% %z% %radius%", "GOV", "%radius%*1", "渲染一定范围内的卫星地图", "radius"));
    }};
    @Ignore
    @Getter
    private static Map<String, Trade> index = new LinkedHashMap<>();

    @Override
    public boolean onLoaded() {
        index = trades.stream().collect(Collectors.toMap(Trade::getId, trade -> trade, (a, b) -> b, LinkedHashMap::new));
        return true;
    }
}

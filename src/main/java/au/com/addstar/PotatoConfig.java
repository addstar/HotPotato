package au.com.addstar;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "hotpotato")
public class PotatoConfig implements ConfigData {
    // Should we broadcast a message when the potato has been exchanged?
    public boolean broadcast_exchange = true;

    // The message to broadcast
    public String exchange_msg = "[\"\","
        + "{\"text\":\"Attention everyone!\",\"color\":\"yellow\",\"bold\":\"true\"},"
        + "{\"text\":\"\n\"},"
        + "{\"text\":\" {to} \",\"color\":\"aqua\"},"
        + "{\"text\":\"received the \",\"color\":\"green\"},"
        + "{\"text\":\"Hot Potato \",\"color\":\"red\"},"
        + "{\"text\":\"from \",\"color\":\"green\"},"
        + "{\"text\":\"{from}\",\"color\":\"aqua\"},"
        + "{\"text\":\"!\",\"color\":\"green\"}]";

    // How long to wait (seconds) before broadcasting the message
    public int broadcast_delay = 2;
}

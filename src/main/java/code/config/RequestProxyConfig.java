package code.config;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpHost;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class RequestProxyConfig {

    private ProxyTypeEnum type;
    private String hostName;
    private Integer port;

    public static RequestProxyConfig create() {
        ConfigSettings settings = Config.readConfig();

        RequestProxyConfig config = new RequestProxyConfig();
        if (settings.getOnProxy()) {
            config.type = ProxyTypeEnum.HttpProxy;
            config.hostName = settings.getProxyHost();
            config.port = settings.getProxyPort();
        } else {
            config.type = ProxyTypeEnum.getDefault();
        }
        return config;
    }

    public void viaProxy(Request request) {
        switch (this.type) {
            case HttpProxy:
                request.viaProxy(new HttpHost(this.hostName, this.port));
                break;
        }
    }

    public DefaultBotOptions buildDefaultBotOptions() {
        switch (this.type) {
            case HttpProxy:
                DefaultBotOptions botOptions = new DefaultBotOptions();

                botOptions.setProxyHost(this.hostName);
                botOptions.setProxyPort(this.port);
                botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
                return botOptions;
        }
        return null;
    }

}

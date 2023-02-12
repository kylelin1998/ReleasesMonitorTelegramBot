package code.config;

import lombok.Data;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.HttpHost;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class RequestProxyConfig {

    private ProxyTypeEnum type;
    private String hostName;
    private Integer port;

    public static RequestProxyConfig createDefault() {
        RequestProxyConfig config = new RequestProxyConfig();
        config.type = ProxyTypeEnum.getDefault();
        return config;
    }
    public static RequestProxyConfig createHttpProxy(String hostName, Integer port) {
        RequestProxyConfig config = new RequestProxyConfig();
        config.type = ProxyTypeEnum.HttpProxy;
        config.hostName = hostName;
        config.port = port;
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

package songbox.house.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import songbox.house.util.Configuration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import static java.net.Proxy.Type.HTTP;
import static java.util.Objects.nonNull;

@Component
public class HTTPConnectionProvider {

    private static final int DEFAULT_CONNECT_TIMEOUT = 10_000;
    private static final int DEFAULT_READ_TIMEOUT = 10_000;

    private final Proxy proxy;

    @Autowired
    public HTTPConnectionProvider(Configuration configuration) {
        if (configuration != null && configuration.getProxy() != null) {
            this.proxy = new Proxy(HTTP, new InetSocketAddress(configuration.getProxy().getIp(), configuration.getProxy().getPort()));
        } else {
            this.proxy = null;
        }
    }

    public HttpURLConnection getConnection(URL url, boolean isProxied) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) (isProxied && nonNull(proxy) ? url.openConnection(proxy) : url.openConnection());
        connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        return connection;
    }
}

package ankoushinist.krswbomb;

import com.google.common.io.ByteStreams;
import org.openqa.selenium.Proxy;

import java.io.*;

public class Shared {
    private Shared() {
    }

    public static Proxy socks5(String proxy) {
        Proxy r = new Proxy();
        r.setSocksProxy(proxy);
        return r;
    }

    public static String extractGeckoDriver() {
        String sysProp = System.getProperty("webdriver.gecko.driver");
        if (sysProp != null) {
            return sysProp;
        }
        try {
            System.out.println("Extracting geckodriver...");
            File tmp = File.createTempFile("gecko", "driver");
            try (InputStream is = Bomb.class.getClassLoader().getResourceAsStream("geckodriver")) {
                try (OutputStream os = new FileOutputStream(tmp)) {
                    ByteStreams.copy(is, os);
                }
            }
            tmp.setExecutable(true);
            String absolute = tmp.getAbsolutePath();
            System.out.println("Extracted at " + absolute);
            return absolute;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

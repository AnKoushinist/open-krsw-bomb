package ankoushinist.krswbomb;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ankoushinist.krswbomb.Shared.extractGeckoDriver;

public class Bomb {
    public static final Pattern REGEX_PARALLEL = Pattern.compile("--parallel=([0-9]+)");
    public static final Pattern REGEX_PROXY = Pattern.compile("--proxy=(.+:[0-9]+)");

    public static void main(String... args) {
        System.setProperty("webdriver.gecko.driver", extractGeckoDriver());
        Config cfg = new Config();
        List<String> unconsumed = new ArrayList<>();
        for (String arg : args) {
            if (REGEX_PARALLEL.matcher(arg).matches()) {
                Matcher matcher = REGEX_PARALLEL.matcher(arg);
                matcher.find();
                cfg.parallel = Integer.parseInt(matcher.group(1));
            } else if (REGEX_PROXY.matcher(arg).matches()) {
                Matcher matcher = REGEX_PROXY.matcher(arg);
                matcher.find();
                cfg.proxy = matcher.group(1);
            } else {
                unconsumed.add(arg);
            }
        }
        cfg.name = unconsumed.get(0);
        cfg.iteration = Integer.parseInt(unconsumed.get(1));
        cfg.check();
        start(cfg);
    }

    public static void start(Config cfg) {
        int[] perThread = new int[cfg.parallel];
        if (cfg.iteration >= 0) {
            Arrays.fill(perThread, cfg.iteration / perThread.length);
            int remaining = cfg.iteration - Arrays.stream(perThread).sum();
            for (int i = 0; i < remaining; i++) {
                perThread[i]++;
            }
        } else {
            Arrays.fill(perThread, -1);
        }
        int count = 0;
        for (int i : perThread) {
            launchThread(cfg.name, i, count++, cfg.proxy);
        }
    }

    private static void launchThread(String name, int count, int id, String proxy) {
        new Thread(() -> {
            String url = "http://candy.am/pc/blog/top.html#!/" + name;
            FirefoxOptions opts = new FirefoxOptions();
            opts.setHeadless(true);
            opts.setProxy(Shared.socks5(proxy));
            for (int i = 0; count == -1 || i < count; i++) {
                FirefoxDriver browser = new FirefoxDriver(opts);
                try {
                    browser.get(url);
                    WebDriverWait wait = new WebDriverWait(browser, 20);
                    wait.until(drv -> browser.executeScript("return document.readyState").equals("complete"));
                } catch (Throwable e) {
                    System.err.println("Error from thread " + id + " at " + count);
                    e.printStackTrace();
                } finally {
                    browser.close();
                    if (i % 10 == 0) {
                        System.out.println("Thread #" + id + ": " + i);
                    }
                }
            }
        }).start();
    }

    public static class Config {
        String name;
        int iteration = Integer.MIN_VALUE;
        int parallel = 1;
        String proxy = null;

        public void check() {
            if (name == null) throw new NullPointerException("name must be specified");
            if (iteration == Integer.MIN_VALUE) throw new IllegalStateException("iteration must be specified");
        }
    }
}

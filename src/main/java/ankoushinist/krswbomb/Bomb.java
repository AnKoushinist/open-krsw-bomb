package ankoushinist.krswbomb;

import okhttp3.OkHttpClient;
import okhttp3.Request;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bomb {
    public static final Pattern REGEX_PARALLEL = Pattern.compile("--parallel=([0-9]+)");
    public static final Pattern REGEX_PROXY = Pattern.compile("--proxy=(.+:[0-9]+)");
    public static final Pattern REGEX_PROXY_EXTRACTION = Pattern.compile("(.+):([0-9]+)");

    public static void main(String... args) {
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
            OkHttpClient client;
            if (proxy != null) {
                client = new OkHttpClient.Builder().proxy(socks5(proxy)).build();
            } else {
                client = new OkHttpClient();
            }
            Request req = new Request.Builder()
                    .url(url)
                    .build();
            for (int i = 0; i < count; i++) {
                try {
                    client.newCall(req).execute().body().string();
                } catch (Throwable e) {
                    System.err.println("Error from thread " + id + " at " + count);
                    e.printStackTrace();
                }
                if (i % 10 == 0) {
                    System.out.println("Thread #" + id + ": " + i);
                }
            }
        }).start();
    }

    private static Proxy socks5(String proxy) {
        Matcher matcher = REGEX_PROXY_EXTRACTION.matcher(proxy);
        matcher.find();
        String proxyHost = matcher.group(1);
        int proxyPort = Integer.parseInt(matcher.group(2));
        InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
        return new Proxy(Proxy.Type.SOCKS, proxyAddr);
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

package ankoushinist.krswbomb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Bomb {
    public static final Pattern REGEX_PARALLEL = Pattern.compile("--parallel=([0-9]+)");
    public static final Pattern REGEX_PROXY = Pattern.compile("--proxy=(.+)");

    public static void main(String... args) {
        Config cfg = new Config();
        List<String> unconsumed = new ArrayList<>();
        for (String arg : args) {
            if (REGEX_PARALLEL.matcher(arg).matches()) {
                cfg.parallel = Integer.parseInt(REGEX_PARALLEL.matcher(arg).group(1));
            } else if (REGEX_PROXY.matcher(arg).matches()) {
                cfg.proxy = REGEX_PROXY.matcher(arg).group(1);
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
    }

    public static class Config {
        String name;
        int iteration = -1;
        int parallel = 1;
        String proxy = null;

        public void check() {
            if (name == null) throw new NullPointerException("name must be specified");
            if (iteration == -1) throw new IllegalStateException("iteration must be specified");
        }
    }
}

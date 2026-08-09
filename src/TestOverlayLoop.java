package com.fpsmoon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class TestOverlayLoop {
    private static final String STATE_PATH = "/data/adb/modules/fps_moon/state/stats.json";

    public static void main(String[] args) throws Exception {
        System.out.println("=== TESTING OVERLAY FILE READ FOR 10 TICKS ===");
        for (int i = 0; i < 10; i++) {
            Map<String, String> stats = readStats();
            System.out.println("Tick " + (i+1) + ": FPS = " + stats.get("fps") + " | FT = " + stats.get("frametime") + " | CPU = " + stats.get("cpu_load") + "% | TS = " + stats.get("timestamp"));
            Thread.sleep(300);
        }
    }

    private static Map<String, String> readStats() {
        Map<String, String> map = new HashMap<>();
        File file = new File(STATE_PATH);
        if (!file.exists()) return map;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim().replace("\"", "").replace(",", "");
                    String val = parts[1].trim().replace("\"", "").replace(",", "");
                    map.put(key, val);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}

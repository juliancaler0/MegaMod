package com.ultra.megamod.feature.ambientsounds.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class SimpleHashMapDouble<K> extends HashMap<K, Double> {

    public void addValue(K key, double amount) {
        merge(key, amount, Double::sum);
    }

    public double getOrDefault(K key, double def) {
        Double val = get(key);
        return val != null ? val : def;
    }

    public String toString(DecimalFormat format) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<K, Double> entry : entrySet()) {
            if (!first)
                sb.append(", ");
            sb.append(entry.getKey()).append("=").append(format.format(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}

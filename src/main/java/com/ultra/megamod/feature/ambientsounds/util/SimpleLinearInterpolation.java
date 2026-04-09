package com.ultra.megamod.feature.ambientsounds.util;

import java.util.List;

public class SimpleLinearInterpolation {

    private final List<SimplePair<Double, Double>> points;

    public SimpleLinearInterpolation(List<SimplePair<Double, Double>> sortedPoints) {
        this.points = sortedPoints;
    }

    public double valueAt(double key) {
        if (points.isEmpty()) {
            return 0.0;
        }

        SimplePair<Double, Double> first = points.getFirst();
        if (key <= first.key()) {
            return first.value();
        }

        SimplePair<Double, Double> last = points.getLast();
        if (key >= last.key()) {
            return last.value();
        }

        for (int i = 0; i < points.size() - 1; i++) {
            SimplePair<Double, Double> a = points.get(i);
            SimplePair<Double, Double> b = points.get(i + 1);

            if (key >= a.key() && key <= b.key()) {
                double t = (key - a.key()) / (b.key() - a.key());
                return a.value() + t * (b.value() - a.value());
            }
        }

        return last.value();
    }
}

package com.mineshinima.mclib.utils;

public class MathUtils {
    public static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static<T extends Comparable> T clamp(T value, T min, T max) {
        return value.compareTo(min) < 0 ? min : (value.compareTo(max) > 0 ? max : value);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static long clamp(long value, long min, long max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static<T extends Comparable> T max(T a, T b) {
        return a.compareTo(b) < 0 ? b : a;
    }

    public static<T extends Comparable> T min(T a, T b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    public static float mapRange(float value, float min, float max, float toMin, float toMax) {
        return mapRange(value, min, max, toMin, toMax, true);
    }

    public static float mapRange(float value, float min, float max, float toMin, float toMax, boolean clamp) {
        return (float) mapRange((double) value, min, max, toMin, toMax, clamp);
    }

    public static double mapRange(double value, double min, double max, double toMin, double toMax) {
        return mapRange(value, min, max, toMin, toMax, true);
    }

    public static double mapRange(double value, double min, double max, double toMin, double toMax, boolean clamp) {
        double converted = (value - min) / (max - min) * (toMax - toMin) + toMin;
        return clamp ? MathUtils.clamp(converted, toMin, toMax) : converted;
    }
}

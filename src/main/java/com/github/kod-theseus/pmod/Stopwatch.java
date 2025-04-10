package com.github.kodtheseus.forgetemplate;

public class Stopwatch {
    private long startTime = 0;
    private boolean running = false;
    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }
    public void stop() {
        if (!running) {
            throw new IllegalStateException("Stopwatch is not running.");
        }
        running = false;
    }
    public double getElapsedTimeInSeconds() {
        if (!running) {
            return 0;
        }
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }
}
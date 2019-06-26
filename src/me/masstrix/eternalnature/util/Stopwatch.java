package me.masstrix.eternalnature.util;

public class Stopwatch {

    private boolean started = false;
    private long start = -1;
    private long end;
    private long runtime;

    public Stopwatch start() {
        this.start = System.currentTimeMillis();
        return this;
    }

    public Stopwatch startIfNew() {
        if (!started) {
            start();
            started = true;
        }
        return this;
    }

    public long stop() {
        this.end = System.currentTimeMillis();
        this.runtime = end - start;
        return runtime;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis() - this.start;
    }

    public long getRuntime() {
        return runtime == 0 ? getCurrentTime() : runtime;
    }

    public boolean hasPassed(long mills) {
        return getCurrentTime() >= mills;
    }

    public void reset() {
        start = -1;
        end = 0;
        runtime = 0;
    }
}

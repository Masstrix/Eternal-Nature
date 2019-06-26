package me.masstrix.eternalnature.util;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory implements ThreadFactory {

    private final String NAME;
    private AtomicInteger threadN = new AtomicInteger(0);

    public SimpleThreadFactory(String name) {
        this.NAME = name;
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        return new Thread(r, String.format("%s-%d", NAME, threadN.getAndAdd(1)));
    }
}

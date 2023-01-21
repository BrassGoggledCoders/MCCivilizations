package xyz.brassgoggledcoders.mccivilizations.network.queue;

import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.mccivilizations.MCCivilizations;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class QueueEntry<T> implements Comparable<QueueEntry<?>> {
    private final T value;
    private final Predicate<T> isReady;
    private final Consumer<T> handle;
    private final int priority;
    private final long startedTick;
    private final CountDownLatch attempts;

    public QueueEntry(T value, Predicate<T> isReady, Consumer<T> handle, int priority, long startedTick) {
        this.value = value;
        this.isReady = isReady;
        this.handle = handle;
        this.priority = priority;
        this.startedTick = startedTick;
        this.attempts = new CountDownLatch(5);
    }

    @Override
    public int compareTo(@NotNull QueueEntry<?> queueEntry) {
        int priorityCompare = Integer.compare(this.priority, queueEntry.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        return Long.compare(this.startedTick, queueEntry.priority);
    }

    public boolean tryRun() {
        this.attempts.countDown();

        if (this.isReady.test(this.value)) {
            this.handle.accept(this.value);
            return true;
        } else {
            boolean giveUp = this.attempts.getCount() == 0;
            if (giveUp) {
                MCCivilizations.LOGGER.warn("Tried 5 Times to Update, Packet wasn't Ready: {}", this.value.toString());
            }
            return giveUp;
        }
    }
}

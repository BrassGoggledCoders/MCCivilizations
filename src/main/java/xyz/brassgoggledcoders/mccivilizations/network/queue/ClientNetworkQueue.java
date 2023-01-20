package xyz.brassgoggledcoders.mccivilizations.network.queue;

import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientNetworkQueue {
    public static final ClientNetworkQueue INSTANCE = new ClientNetworkQueue();

    private final PriorityQueue<QueueEntry<?>> priorityQueue;

    private int tickWaiting = 0;
    private long tick = 0;

    public ClientNetworkQueue() {
        this.priorityQueue = new PriorityQueue<>(QueueEntry::compareTo);
    }

    public void doQueueTick() {
        tick++;
        if (--tickWaiting <= 0) {
            QueueEntry<?> queueEntry = priorityQueue.poll();
            int handled = 0;
            boolean ready = true;
            while (queueEntry != null && handled < 5 && ready) {
                ready = queueEntry.tryRun();
                handled++;
                if (ready) {
                    queueEntry = priorityQueue.poll();
                } else {
                    priorityQueue.add(queueEntry);
                }

            }

            tickWaiting = 10;
        }
    }

    public <T> void queue(T entry, Predicate<T> isReady, Consumer<T> handle, int priority) {
        this.priorityQueue.add(new QueueEntry<T>(
                entry,
                isReady,
                handle,
                priority,
                tick
        ));
    }

    public static ClientNetworkQueue getInstance() {
        return INSTANCE;
    }
}

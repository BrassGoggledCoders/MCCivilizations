package xyz.brassgoggledcoders.mccivilizations.network.queue;

import java.util.PriorityQueue;

public class ClientNetworkQueue {
    public static final ClientNetworkQueue INSTANCE = new ClientNetworkQueue();

    private final PriorityQueue<QueueEntry<?>> priorityQueue;

    private int tickWaiting = 0;

    public ClientNetworkQueue() {
        this.priorityQueue = new PriorityQueue<>(QueueEntry::compareTo);
    }

    public void doQueueTick() {
        if (--tickWaiting <= 0) {
            QueueEntry<?> queueEntry = priorityQueue.poll();
            int handled = 0;
            boolean ready = true;
            while (queueEntry != null && handled < 5 && ready) {
                ready = queueEntry.tryRun();
                handled++;
                queueEntry = priorityQueue.poll();
            }

            tickWaiting = 10;
        }
    }

    public static ClientNetworkQueue getInstance() {
        return INSTANCE;
    }
}

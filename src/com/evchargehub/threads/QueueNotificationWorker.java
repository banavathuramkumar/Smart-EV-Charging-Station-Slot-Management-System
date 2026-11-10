package com.evchargehub.threads;

import com.evchargehub.collections.WaitingQueue;
import com.evchargehub.model.QueueEntry;

/**
 * Background worker task implementing Runnable to monitor the waiting queue.
 * - Creating threads using the Runnable interface
 * - Background execution
 * - Inter-thread communication coordination
 */
public class QueueNotificationWorker implements Runnable {
    public interface QueueNotificationListener {
        void onUserNotified(QueueEntry entry);
    }

    private final WaitingQueue waitingQueue;
    private final QueueNotificationListener listener;
    private volatile boolean running = true;

    public QueueNotificationWorker(WaitingQueue waitingQueue, QueueNotificationListener listener) {
        this.waitingQueue = waitingQueue;
        this.listener = listener;
    }

    @Override
    public void run() {
        System.out.println("[QueueWorker for Station " + waitingQueue.getStationId() + "] Thread started: " + Thread.currentThread().getName());
        while (running) {
            try {
                // Check if queue has entries
                if (!waitingQueue.isEmpty()) {
                    // Peek or handle wait
                    Thread.sleep(2000);
                } else {
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                System.out.println("[QueueWorker] Interrupted.");
                break;
            }
        }
        System.out.println("[QueueWorker for Station " + waitingQueue.getStationId() + "] Thread stopped.");
    }

    public void stopWorker() {
        this.running = false;
    }
}

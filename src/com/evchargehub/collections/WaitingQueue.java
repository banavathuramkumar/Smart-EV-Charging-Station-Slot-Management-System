package com.evchargehub.collections;

import com.evchargehub.model.QueueEntry;
import java.util.LinkedList;
import java.util.List;

/**
 * Waiting Queue implementation for stations when all charging slots are occupied.
 * Demonstrates:
 * - LinkedList collection class and List interface
 * - Multithreading, Synchronization, and Inter-thread Communication (wait and notifyAll)
 */
public class WaitingQueue {
    // LinkedList to maintain strict First-In, First-Out (FIFO) queue order
    private final LinkedList<QueueEntry> queue;
    private final int stationId;
    private final int capacity;

    public WaitingQueue(int stationId, int capacity) {
        this.stationId = stationId;
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    /**
     * Enqueue a user into the waiting list.
     * Synchronized method with wait() if capacity is full.
     */
    public synchronized boolean enqueue(QueueEntry entry) throws InterruptedException {
        while (queue.size() >= capacity) {
            System.out.println("[WaitingQueue Station " + stationId + "] Queue is full. User " + entry.getUserId() + " waiting for queue spot...");
            wait(3000); // Wait up to 3 seconds or until notified
            if (queue.size() >= capacity) {
                return false; // Still full
            }
        }
        queue.addLast(entry);
        System.out.println("[WaitingQueue Station " + stationId + "] User " + entry.getUserId() + " added to queue. Position: " + queue.size());
        notifyAll(); // Notify any waiting threads
        return true;
    }

    /**
     * Dequeue the next eligible user when a slot becomes available.
     * Uses inter-thread communication notifyAll() to wake waiting threads.
     */
    public synchronized QueueEntry dequeueNext() {
        if (queue.isEmpty()) {
            return null;
        }
        QueueEntry next = queue.removeFirst();
        next.setNotified(true);
        System.out.println("[WaitingQueue Station " + stationId + "] Slot freed! Notified user: " + next.getUserName() + " (ID: " + next.getUserId() + ")");
        notifyAll(); // Notify waiting producers/threads
        return next;
    }

    /**
     * Remove a specific user who chose to leave the queue.
     */
    public synchronized boolean removeUser(int userId) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getUserId() == userId) {
                QueueEntry removed = queue.remove(i);
                System.out.println("[WaitingQueue Station " + stationId + "] User " + removed.getUserName() + " voluntarily left the queue.");
                notifyAll();
                return true;
            }
        }
        return false;
    }

    /**
     * Peek at the next user in line without removing.
     */
    public synchronized QueueEntry peek() {
        return queue.peekFirst();
    }

    /**
     * Get a snapshot of all queued entries.
     */
    public synchronized List<QueueEntry> getAllEntries() {
        return new LinkedList<>(queue);
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getStationId() {
        return stationId;
    }
}

package com.evchargehub.threads;

import com.evchargehub.model.ChargingSession;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Multithreaded background worker simulating an active EV Charging Session.
 * - Creating threads by extending the Thread class
 * - Thread life cycle (NEW -> RUNNABLE -> TIMED_WAITING -> TERMINATED)
 * - Thread priorities (setPriority)
 * - Thread synchronization
 * - Session update callback without blocking the GUI thread
 */
public class ChargingSessionThread extends Thread {
    public interface SessionUpdateListener {
        void onProgress(double currentEnergyKWh, double currentCost, int elapsedSeconds, double batteryPercentage);
        void onComplete(ChargingSession completedSession);
        void onError(String errorMessage);
    }

    private final ChargingSession session;
    private final double chargingPowerKW;
    private final double pricePerKWh;
    private final double targetKWh;
    private final double initialBatteryPercentage;
    private final double vehicleCapacityKWh;
    private final SessionUpdateListener listener;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean completedOrStopped = new AtomicBoolean(false);
    private int elapsedSeconds = 0;

    public ChargingSessionThread(ChargingSession session, 
                                 double chargingPowerKW, 
                                 double pricePerKWh, 
                                 double vehicleCapacityKWh,
                                 double currentBatteryPercentage,
                                 SessionUpdateListener listener) {
        super("ChargingSessionThread-Session-" + session.getSessionId());
        this.session = session;
        this.chargingPowerKW = chargingPowerKW;
        this.pricePerKWh = pricePerKWh;
        this.vehicleCapacityKWh = vehicleCapacityKWh > 0 ? vehicleCapacityKWh : 60.0;
        this.initialBatteryPercentage = currentBatteryPercentage;
        this.listener = listener;

        // Calculate target energy to reach 100%
        double neededKWh = this.vehicleCapacityKWh * (Math.max(0.0, 100.0 - currentBatteryPercentage) / 100.0);
        this.targetKWh = neededKWh > 0 ? neededKWh : 20.0;

        // Set thread priority
        this.setPriority(Thread.NORM_PRIORITY + 1);
        this.setDaemon(true); // Daemon thread so it terminates on application exit
    }

    @Override
    public void run() {
        running.set(true);
        System.out.println("[Thread: " + getName() + "] Started with Priority " + getPriority() + ". State: " + getState());

        double deliveredEnergy = 0.0;
        // In simulation, 1 real second represents 60 seconds (1 minute) of fast charging
        // Energy per real second = (chargingPowerKW / 3600.0) * timeAccelerationFactor
        final double timeAccelerationFactor = 60.0; // 1 second = 1 simulated minute
        final double kwhPerTick = (chargingPowerKW / 3600.0) * timeAccelerationFactor;

        try {
            while (running.get() && deliveredEnergy < targetKWh) {
                // Synchronization lock for pause/resume control
                synchronized (this) {
                    while (paused.get()) {
                        wait(); // Inter-thread communication
                    }
                }

                Thread.sleep(1000); // 1-second simulation tick
                elapsedSeconds++;

                deliveredEnergy += kwhPerTick;
                if (deliveredEnergy > targetKWh) {
                    deliveredEnergy = targetKWh;
                }

                double currentCost = deliveredEnergy * pricePerKWh;
                double addedPercentage = (deliveredEnergy / vehicleCapacityKWh) * 100.0;
                double currentBatteryPct = Math.min(100.0, initialBatteryPercentage + addedPercentage);

                // Update session object
                session.setEnergyDeliveredKWh(deliveredEnergy);
                session.setTotalCost(currentCost);

                if (listener != null) {
                    listener.onProgress(deliveredEnergy, currentCost, elapsedSeconds, currentBatteryPct);
                }
            }

            // Session completed normally
            if (completedOrStopped.compareAndSet(false, true)) {
                synchronized (this) {
                    session.setStatus("COMPLETED");
                    session.setEndTime(java.time.LocalTime.now().toString().substring(0, 8));
                }

                if (listener != null) {
                    listener.onComplete(session);
                }
            }

        } catch (InterruptedException e) {
            System.out.println("[Thread: " + getName() + "] Interrupted/Stopped by user.");
            if (completedOrStopped.compareAndSet(false, true)) {
                session.setStatus("STOPPED");
                session.setEndTime(java.time.LocalTime.now().toString().substring(0, 8));
                if (listener != null) {
                    listener.onComplete(session);
                }
            }
        } catch (Exception e) {
            System.err.println("[Thread: " + getName() + "] Error: " + e.getMessage());
            if (listener != null) {
                listener.onError(e.getMessage());
            }
        } finally {
            running.set(false);
            System.out.println("[Thread: " + getName() + "] Finished. Final State: " + getState());
        }
    }

    public synchronized void pauseSession() {
        paused.set(true);
    }

    public synchronized void resumeSession() {
        paused.set(false);
        notifyAll(); // Wake up waiting thread
    }

    public void stopCharging() {
        running.set(false);
        this.interrupt();
    }

    public boolean isChargingActive() {
        return running.get() && isAlive();
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }
}

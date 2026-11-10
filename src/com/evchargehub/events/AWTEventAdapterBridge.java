package com.evchargehub.events;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Demonstrates Event Handling concepts:
 * - Delegation Event Model
 * - Event classes (KeyEvent, ActionEvent)
 * - Event Listener interfaces (KeyListener, ActionListener)
 * - Adapter classes (KeyAdapter)
 */
public class AWTEventAdapterBridge {

    public interface ActionTrigger {
        void execute();
    }

    /**
     * Custom KeyAdapter demonstrating:
     * - Subclassing an AWT Adapter class instead of implementing all methods of KeyListener
     * - Handling KeyEvent (keyPressed, keyTyped)
     * - Delegation Event Model
     */
    public static class ShortcutKeyAdapter extends KeyAdapter {
        private final ActionTrigger onEnterAction;
        private final ActionTrigger onEscapeAction;

        public ShortcutKeyAdapter(ActionTrigger onEnterAction, ActionTrigger onEscapeAction) {
            this.onEnterAction = onEnterAction;
            this.onEscapeAction = onEscapeAction;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            System.out.println("[AWT Event Model] Key Pressed detected: " + KeyEvent.getKeyText(keyCode) + " (Code: " + keyCode + ")");

            if (keyCode == KeyEvent.VK_ENTER && onEnterAction != null) {
                System.out.println("[AWT Event Model] Delegating VK_ENTER action...");
                onEnterAction.execute();
            } else if (keyCode == KeyEvent.VK_ESCAPE && onEscapeAction != null) {
                System.out.println("[AWT Event Model] Delegating VK_ESCAPE action...");
                onEscapeAction.execute();
            }
        }
    }

    /**
     * Demonstrates an ActionListener implementation using the Delegation Event Model.
     */
    public static class SimulationActionListener implements ActionListener {
        private final String actionName;
        private final ActionTrigger trigger;

        public SimulationActionListener(String actionName, ActionTrigger trigger) {
            this.actionName = actionName;
            this.trigger = trigger;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("[AWT Event Model] ActionEvent fired: " + actionName + " from source: " + e.getSource());
            if (trigger != null) {
                trigger.execute();
            }
        }
    }

    /**
     * Utility method to test and demonstrate event delegation.
     */
    public static void testDelegationModel() {
        System.out.println("\n--- Testing AWT Delegation Event Model ---");
        // Create an anonymous dummy component as the event source
        Component dummySource = new Component() {};

        // Register adapter
        KeyAdapter adapter = new ShortcutKeyAdapter(
            () -> System.out.println("  [Executed] Enter triggered successfully!"),
            () -> System.out.println("  [Executed] Escape triggered successfully!")
        );

        // Simulate KeyEvent for Enter (VK_ENTER)
        KeyEvent enterEvent = new KeyEvent(dummySource, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');
        adapter.keyPressed(enterEvent);

        // Simulate ActionEvent
        ActionListener actionListener = new SimulationActionListener("SIMULATE_BOOKING_TRIGGER", () -> {
            System.out.println("  [Executed] Action listener triggered successfully!");
        });
        ActionEvent actionEvent = new ActionEvent(dummySource, ActionEvent.ACTION_PERFORMED, "CONFIRM");
        actionListener.actionPerformed(actionEvent);
    }
}

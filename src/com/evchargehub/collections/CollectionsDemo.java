package com.evchargehub.collections;

import com.evchargehub.model.ChargingStation;
import java.util.*;

/**
 * Demonstrates Collections Framework concepts:
 * - Collection Interfaces: List, Set, Map
 * - Collection Classes: ArrayList, LinkedList, Vector, HashSet, TreeSet, Hashtable, HashMap
 * - Accessing collections via an Iterator
 */
public class CollectionsDemo {

    /**
     * Demonstrates List interface with ArrayList, LinkedList, and Vector.
     */
    public static void demonstrateLists() {
        System.out.println("\n========== LIST DEMONSTRATION ==========");
        
        // 1. ArrayList - fast random access
        List<String> connectorList = new ArrayList<>();
        connectorList.add("CCS2 (Combined Charging System 2)");
        connectorList.add("Type 2 (Mennekes)");
        connectorList.add("CHAdeMO (DC Fast)");
        connectorList.add("GB/T (Fast DC)");

        System.out.println("1. ArrayList of Connectors (via Iterator):");
        Iterator<String> arrayIterator = connectorList.iterator();
        while (arrayIterator.hasNext()) {
            System.out.println("   -> " + arrayIterator.next());
        }

        // 2. LinkedList - doubly-linked list for queue operations
        LinkedList<String> dispatchLog = new LinkedList<>();
        dispatchLog.addFirst("Dispatch-001: Station Initialized");
        dispatchLog.addLast("Dispatch-002: Voltage Regulated 400V");
        dispatchLog.addLast("Dispatch-003: Vehicle Handshake Established");

        System.out.println("2. LinkedList (FIFO/LIFO Operations):");
        for (String log : dispatchLog) {
            System.out.println("   [LOG] " + log);
        }

        // 3. Vector - synchronized legacy dynamic array
        Vector<String> telemetryAuditVector = new Vector<>();
        telemetryAuditVector.add("Telemetry-Audit-Alpha");
        telemetryAuditVector.add("Telemetry-Audit-Beta");
        telemetryAuditVector.add("Telemetry-Audit-Gamma");

        System.out.println("3. Vector (Synchronized Collection via Enumeration/Iterator):");
        Enumeration<String> vEnum = telemetryAuditVector.elements();
        while (vEnum.hasMoreElements()) {
            System.out.println("   (Vector Element) " + vEnum.nextElement());
        }
    }

    /**
     * Demonstrates Set interface with HashSet and TreeSet.
     */
    public static void demonstrateSets() {
        System.out.println("\n========== SET DEMONSTRATION ==========");

        // 4. HashSet - unordered, no duplicate elements
        Set<String> uniqueFeatures = new HashSet<>();
        uniqueFeatures.add("Solar Powered Canopy");
        uniqueFeatures.add("24/7 Security CCTV");
        uniqueFeatures.add("Coffee Lounge & Wi-Fi");
        uniqueFeatures.add("Solar Powered Canopy"); // Duplicate, will not be added

        System.out.println("4. HashSet (Unordered Unique Amenities, size=" + uniqueFeatures.size() + "):");
        Iterator<String> setIterator = uniqueFeatures.iterator();
        while (setIterator.hasNext()) {
            System.out.println("   * " + setIterator.next());
        }

        // 5. TreeSet - sorted set based on natural ordering (Comparable)
        TreeSet<ChargingStation> sortedStations = new TreeSet<>();
        sortedStations.add(new ChargingStation("Suryapet Midway SuperHub", "Suryapet", 8, 240.0, 17.0, "ACTIVE"));
        sortedStations.add(new ChargingStation("Vijayawada Benz Circle", "Vijayawada", 4, 150.0, 16.5, "ACTIVE"));
        sortedStations.add(new ChargingStation("LB Nagar Transit Hub", "Hyderabad", 10, 150.0, 17.5, "ACTIVE"));

        System.out.println("5. TreeSet (Sorted Charging Stations by Location & Name):");
        Iterator<ChargingStation> treeIterator = sortedStations.iterator();
        while (treeIterator.hasNext()) {
            System.out.println("   # " + treeIterator.next());
        }
    }

    /**
     * Demonstrates Map interface with HashMap and Hashtable.
     */
    public static void demonstrateMaps() {
        System.out.println("\n========== MAP DEMONSTRATION ==========");

        // 6. HashMap - fast key-value pairs, allows null keys/values
        Map<String, Double> tariffMap = new HashMap<>();
        tariffMap.put("AC_SLOW", 9.50);
        tariffMap.put("DC_FAST_60KW", 14.00);
        tariffMap.put("DC_ULTRA_120KW", 19.50);

        System.out.println("6. HashMap (Tariff mapping):");
        Iterator<Map.Entry<String, Double>> mapIterator = tariffMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, Double> entry = mapIterator.next();
            System.out.println("   Key=" + entry.getKey() + " -> Price/kWh = Rs." + entry.getValue());
        }

        // 7. Hashtable - legacy synchronized key-value store, no null keys or values
        Hashtable<String, String> stationStatusCache = new Hashtable<>();
        stationStatusCache.put("STN-01", "ONLINE_HEALTHY");
        stationStatusCache.put("STN-02", "OFFLINE_MAINTENANCE");
        stationStatusCache.put("STN-03", "ONLINE_FULL_CAPACITY");

        System.out.println("7. Hashtable (Synchronized Station Status Cache):");
        for (String key : stationStatusCache.keySet()) {
            System.out.println("   [Hashtable Key] " + key + " => " + stationStatusCache.get(key));
        }
    }

    public static void main(String[] args) {
        demonstrateLists();
        demonstrateSets();
        demonstrateMaps();
    }
}

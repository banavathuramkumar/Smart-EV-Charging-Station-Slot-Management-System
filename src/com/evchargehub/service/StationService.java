package com.evchargehub.service;

import com.evchargehub.dao.SlotDAO;
import com.evchargehub.dao.StationDAO;
import com.evchargehub.exceptions.StationException;
import com.evchargehub.model.ChargingSlot;
import com.evchargehub.model.ChargingStation;
import com.evchargehub.model.SlotStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing Charging Stations and Slots.
 */
public class StationService {

    private final StationDAO stationDAO;
    private final SlotDAO slotDAO;

    public StationService() {
        this.stationDAO = new StationDAO();
        this.slotDAO = new SlotDAO();
    }

    public List<ChargingStation> getAllStations() throws SQLException {
        return stationDAO.findAll();
    }

    public List<ChargingStation> getActiveStations() throws SQLException {
        return stationDAO.findActiveStations();
    }

    public ChargingStation getStationById(int stationId) throws SQLException {
        return stationDAO.findById(stationId);
    }

    public boolean addStation(String name, String location, double speedKW, double pricePerKWh) throws StationException, SQLException {
        if (name == null || name.trim().isEmpty()) throw new StationException("Station name cannot be empty.");
        if (location == null || location.trim().isEmpty()) throw new StationException("Location cannot be empty.");
        if (speedKW <= 0) throw new StationException("Charging speed must be greater than 0 kW.");
        if (pricePerKWh <= 0) throw new StationException("Price per kWh must be greater than $0.00.");

        ChargingStation s = new ChargingStation(name.trim(), location.trim(), 0, speedKW, pricePerKWh, "ACTIVE");
        return stationDAO.insert(s);
    }

    public boolean updateStation(ChargingStation station) throws StationException, SQLException {
        if (station.getName() == null || station.getName().trim().isEmpty()) throw new StationException("Station name cannot be empty.");
        return stationDAO.update(station);
    }

    public boolean toggleStationStatus(int stationId) throws SQLException {
        ChargingStation s = stationDAO.findById(stationId);
        if (s == null) return false;
        String newStatus = "ACTIVE".equalsIgnoreCase(s.getStatus()) ? "INACTIVE" : "ACTIVE";
        return stationDAO.updateStatus(stationId, newStatus);
    }

    public boolean deleteStation(int stationId) throws StationException, SQLException {
        // Check if bookings exist for this station
        String checkBookingsSql = "SELECT COUNT(*) FROM bookings WHERE station_id = ?";
        try (var conn = com.evchargehub.database.DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(checkBookingsSql)) {
            ps.setInt(1, stationId);
            try (var rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new StationException("Cannot delete station because " + rs.getInt(1) + " booking record(s) exist for it. You can deactivate it by toggling its status to INACTIVE instead.");
                }
            }
        }
        return stationDAO.delete(stationId);
    }

    // --- Slot Operations ---

    public List<ChargingSlot> getSlotsByStation(int stationId) throws SQLException {
        return slotDAO.findByStationId(stationId);
    }

    public List<ChargingSlot> getAvailableSlots(int stationId) throws SQLException {
        return slotDAO.findAvailableByStationId(stationId);
    }

    public boolean addSlot(int stationId, String slotNumber, String connectorType, double powerKW) throws StationException, SQLException {
        if (slotNumber == null || slotNumber.trim().isEmpty()) throw new StationException("Slot number cannot be empty.");
        if (powerKW <= 0) throw new StationException("Slot power must be greater than 0 kW.");

        ChargingSlot slot = new ChargingSlot(stationId, slotNumber.trim().toUpperCase(), connectorType, powerKW, SlotStatus.AVAILABLE);
        boolean ok = slotDAO.insert(slot);
        if (ok) {
            stationDAO.recalculateAvailableSlots(stationId);
        }
        return ok;
    }

    public boolean updateSlotStatus(int slotId, SlotStatus status, int stationId) throws SQLException {
        boolean ok = slotDAO.updateStatus(slotId, status);
        if (ok && stationId > 0) {
            stationDAO.recalculateAvailableSlots(stationId);
        }
        return ok;
    }

    public boolean deleteSlot(int slotId, int stationId) throws SQLException {
        boolean ok = slotDAO.delete(slotId);
        if (ok && stationId > 0) {
            stationDAO.recalculateAvailableSlots(stationId);
        }
        return ok;
    }
}

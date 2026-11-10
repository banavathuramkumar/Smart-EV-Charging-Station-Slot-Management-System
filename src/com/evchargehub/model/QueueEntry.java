package com.evchargehub.model;

/**
 * Represents an entry in the Station Waiting Queue.
 */
public class QueueEntry {
    private int queueId;
    private int userId;
    private int stationId;
    private String userName;
    private String stationName;
    private String userEmail;
    private String joinTime;
    private boolean notified;

    public QueueEntry() {}

    public QueueEntry(int userId, int stationId, String joinTime) {
        this(0, userId, stationId, "", "", "", joinTime, false);
    }

    public QueueEntry(int queueId, int userId, int stationId, String userName, String stationName, String userEmail, String joinTime, boolean notified) {
        this.queueId = queueId;
        this.userId = userId;
        this.stationId = stationId;
        this.userName = userName;
        this.stationName = stationName;
        this.userEmail = userEmail;
        this.joinTime = joinTime;
        this.notified = notified;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(String joinTime) {
        this.joinTime = joinTime;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    @Override
    public String toString() {
        return "QueueEntry #" + queueId + ": User " + userName + " for Station " + stationName + " (Joined: " + joinTime + ")";
    }
}

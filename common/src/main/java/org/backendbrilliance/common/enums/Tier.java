package org.backendbrilliance.common.enums;

public enum Tier {

    FREE(1, 100, 1),
    PRO(10, -1, 30),
    TEAM(50, -1, 90);

    public final int maxEndpoints;
    public final int maxRequestsPerDay; // -1 = unlimited
    public final int historyDays;

    Tier(int maxEndpoints, int maxRequestsPerDay, int historyDays) {
        this.maxEndpoints = maxEndpoints;
        this.maxRequestsPerDay = maxRequestsPerDay;
        this.historyDays = historyDays;
    }
}
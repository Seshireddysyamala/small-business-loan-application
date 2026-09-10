package com.seshi.loanapplication.messaging.event;

public enum CreditRiskBand {
    LOW,
    MEDIUM,
    HIGH;

    public static CreditRiskBand fromScore(int creditScore) {
        if (creditScore >= 740) {
            return LOW;
        }
        if (creditScore >= 670) {
            return MEDIUM;
        }
        return HIGH;
    }
}

package com.safework.api.domain.asset.model;

public enum AssetStatus {
    ACTIVE,
    INACTIVE,
    UNDER_MAINTENANCE,
    DECOMMISSIONED;

    public boolean isOperational() {
        return this == ACTIVE;
    }
}
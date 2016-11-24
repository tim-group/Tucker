package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Report;

public class BroadcastingStatusUpdated implements StatusUpdated {
    private final StatusUpdated[] listeners;

    private BroadcastingStatusUpdated(StatusUpdated... listeners) {
        this.listeners = listeners;
    }

    @Override
    public void accept(Report report) {
        for (StatusUpdated listener : listeners) {
            listener.accept(report);
        }
    }

    public static StatusUpdated broadcastingTo(StatusUpdated... listeners) {
        return new BroadcastingStatusUpdated(listeners);
    }
}
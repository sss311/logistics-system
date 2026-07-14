package com.logistics.service;

public interface SystemService {
    void reset();
    int getClock();
    int tickClock(int steps);
}
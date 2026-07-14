package com.logistics.service;

import com.logistics.model.Claim;
import java.util.List;

public interface ClaimService {
    Claim getById(String id);
    List<Claim> getAll();
    void updateStatus(String id, String status);
    Claim assess(String id, int amount);
    Claim pay(String id);
    Claim reject(String id);
}
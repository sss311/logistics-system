package com.logistics.controller;

import com.logistics.model.Claim;
import com.logistics.service.ClaimService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        return claimService.getAll();
    }

    @GetMapping("/{id}")
    public Claim getClaimById(@PathVariable String id) {
        return claimService.getById(id);
    }

    @PostMapping("/{id}/assess")
    public Claim assess(@PathVariable String id, @RequestBody Map<String, Integer> request) {
        int amount = request.getOrDefault("amount", 0);
        return claimService.assess(id, amount);
    }

    @PostMapping("/{id}/pay")
    public Claim pay(@PathVariable String id) {
        return claimService.pay(id);
    }

    @PostMapping("/{id}/reject")
    public Claim reject(@PathVariable String id) {
        return claimService.reject(id);
    }
}
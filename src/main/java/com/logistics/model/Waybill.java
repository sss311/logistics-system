package com.logistics.model;

public class Waybill {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParcelId() {
        return parcelId;
    }

    public void setParcelId(String parcelId) {
        this.parcelId = parcelId;
    }

    public String getRouteLegs() {
        return routeLegs;
    }

    public void setRouteLegs(String routeLegs) {
        this.routeLegs = routeLegs;
    }

    public String getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(String currentStation) {
        this.currentStation = currentStation;
    }

    public String getDelivTarget() {
        return delivTarget;
    }

    public void setDelivTarget(String delivTarget) {
        this.delivTarget = delivTarget;
    }

    public Integer getPromisedAt() {
        return promisedAt;
    }

    public void setPromisedAt(Integer promisedAt) {
        this.promisedAt = promisedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String parcelId;
    private String routeLegs;
    private String currentStation;
    private String delivTarget;
    private Integer promisedAt;
    private String status;

    // getter/setter 待生成
}
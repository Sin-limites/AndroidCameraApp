package com.sinlimites.objects;

import java.sql.Timestamp;

public class ContainerLocation {

    private int locationID;

    private Container equipmentNumber;

    private Double longitude;
    private Double latitude;
    private Timestamp date;

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public Container getEquipmentNumber() {
        return equipmentNumber;
    }

    public void setEquipmentNumber(Container equipmentNumber) {
        this.equipmentNumber = equipmentNumber;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}

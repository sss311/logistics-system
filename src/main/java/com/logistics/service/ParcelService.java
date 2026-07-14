package com.logistics.service;

import com.logistics.model.Claim;
import com.logistics.model.Parcel;
import java.util.List;

public interface ParcelService {
    Parcel createParcel(String id, String sender, String receiver, String category,
                        Integer declaredValue, String priority, Boolean cod,
                        Integer codAmount, String address, String delivTarget);

    Parcel pickup(String parcelId, String waybillNo);

    Parcel sort(String parcelId, String zone);

    Parcel advance(String parcelId);

    Parcel assignCourier(String parcelId);

    Parcel getById(String id);

    List<Parcel> getAll();

    Parcel deliver(String parcelId);

    Parcel sign(String parcelId);

    Parcel reject(String parcelId);

    Parcel redirect(String parcelId, String delivTarget);

    Claim reportDamage(String parcelId, String degree);

    Parcel deliverFail(String parcelId);
}
package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.enumeration.RideStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Ride} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RideDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime scheduledTime;

    @NotNull
    private RideStatus status;

    @NotNull
    @Size(min = 5)
    private String pickupAddress;

    @NotNull
    @Size(min = 5)
    private String dropoffAddress;

    @NotNull
    private ChildDTO child;

    @NotNull
    private DriverDTO driver;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(ZonedDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public void setDropoffAddress(String dropoffAddress) {
        this.dropoffAddress = dropoffAddress;
    }

    public ChildDTO getChild() {
        return child;
    }

    public void setChild(ChildDTO child) {
        this.child = child;
    }

    public DriverDTO getDriver() {
        return driver;
    }

    public void setDriver(DriverDTO driver) {
        this.driver = driver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RideDTO)) {
            return false;
        }

        RideDTO rideDTO = (RideDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, rideDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RideDTO{" +
            "id=" + getId() +
            ", scheduledTime='" + getScheduledTime() + "'" +
            ", status='" + getStatus() + "'" +
            ", pickupAddress='" + getPickupAddress() + "'" +
            ", dropoffAddress='" + getDropoffAddress() + "'" +
            ", child=" + getChild() +
            ", driver=" + getDriver() +
            "}";
    }
}

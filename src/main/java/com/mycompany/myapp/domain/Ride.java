package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mycompany.myapp.domain.enumeration.RideStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Ride.
 */
@Entity
@Table(name = "ride")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Ride implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "scheduled_time", nullable = false)
    private ZonedDateTime scheduledTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RideStatus status;

    @NotNull
    @Size(min = 5)
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @NotNull
    @Size(min = 5)
    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "parent" }, allowSetters = true)
    private Child child;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "rides" }, allowSetters = true)
    private Driver driver;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Ride id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getScheduledTime() {
        return this.scheduledTime;
    }

    public Ride scheduledTime(ZonedDateTime scheduledTime) {
        this.setScheduledTime(scheduledTime);
        return this;
    }

    public void setScheduledTime(ZonedDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public RideStatus getStatus() {
        return this.status;
    }

    public Ride status(RideStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getPickupAddress() {
        return this.pickupAddress;
    }

    public Ride pickupAddress(String pickupAddress) {
        this.setPickupAddress(pickupAddress);
        return this;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropoffAddress() {
        return this.dropoffAddress;
    }

    public Ride dropoffAddress(String dropoffAddress) {
        this.setDropoffAddress(dropoffAddress);
        return this;
    }

    public void setDropoffAddress(String dropoffAddress) {
        this.dropoffAddress = dropoffAddress;
    }

    public Child getChild() {
        return this.child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public Ride child(Child child) {
        this.setChild(child);
        return this;
    }

    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Ride driver(Driver driver) {
        this.setDriver(driver);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ride)) {
            return false;
        }
        return getId() != null && getId().equals(((Ride) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Ride{" +
            "id=" + getId() +
            ", scheduledTime='" + getScheduledTime() + "'" +
            ", status='" + getStatus() + "'" +
            ", pickupAddress='" + getPickupAddress() + "'" +
            ", dropoffAddress='" + getDropoffAddress() + "'" +
            "}";
    }
}

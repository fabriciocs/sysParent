package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RideTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Ride getRideSample1() {
        return new Ride().id(1L).pickupAddress("pickupAddress1").dropoffAddress("dropoffAddress1");
    }

    public static Ride getRideSample2() {
        return new Ride().id(2L).pickupAddress("pickupAddress2").dropoffAddress("dropoffAddress2");
    }

    public static Ride getRideRandomSampleGenerator() {
        return new Ride()
            .id(longCount.incrementAndGet())
            .pickupAddress(UUID.randomUUID().toString())
            .dropoffAddress(UUID.randomUUID().toString());
    }
}

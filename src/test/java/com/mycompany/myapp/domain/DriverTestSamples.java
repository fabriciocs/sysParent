package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DriverTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Driver getDriverSample1() {
        return new Driver().id(1L).name("name1").phone("phone1").email("email1").licenseNumber("licenseNumber1");
    }

    public static Driver getDriverSample2() {
        return new Driver().id(2L).name("name2").phone("phone2").email("email2").licenseNumber("licenseNumber2");
    }

    public static Driver getDriverRandomSampleGenerator() {
        return new Driver()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .licenseNumber(UUID.randomUUID().toString());
    }
}

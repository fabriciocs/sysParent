package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ChildTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Child getChildSample1() {
        return new Child().id(1L).name("name1").age(1).schoolName("schoolName1");
    }

    public static Child getChildSample2() {
        return new Child().id(2L).name("name2").age(2).schoolName("schoolName2");
    }

    public static Child getChildRandomSampleGenerator() {
        return new Child()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .age(intCount.incrementAndGet())
            .schoolName(UUID.randomUUID().toString());
    }
}

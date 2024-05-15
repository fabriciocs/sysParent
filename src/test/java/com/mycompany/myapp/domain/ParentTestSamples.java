package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ParentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Parent getParentSample1() {
        return new Parent().id(1L).name("name1").phone("phone1").email("email1").address("address1");
    }

    public static Parent getParentSample2() {
        return new Parent().id(2L).name("name2").phone("phone2").email("email2").address("address2");
    }

    public static Parent getParentRandomSampleGenerator() {
        return new Parent()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString());
    }
}

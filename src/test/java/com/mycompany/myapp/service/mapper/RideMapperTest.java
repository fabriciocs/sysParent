package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.RideAsserts.*;
import static com.mycompany.myapp.domain.RideTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RideMapperTest {

    private RideMapper rideMapper;

    @BeforeEach
    void setUp() {
        rideMapper = new RideMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRideSample1();
        var actual = rideMapper.toEntity(rideMapper.toDto(expected));
        assertRideAllPropertiesEquals(expected, actual);
    }
}

package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.ChildTestSamples.*;
import static com.mycompany.myapp.domain.DriverTestSamples.*;
import static com.mycompany.myapp.domain.RideTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RideTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Ride.class);
        Ride ride1 = getRideSample1();
        Ride ride2 = new Ride();
        assertThat(ride1).isNotEqualTo(ride2);

        ride2.setId(ride1.getId());
        assertThat(ride1).isEqualTo(ride2);

        ride2 = getRideSample2();
        assertThat(ride1).isNotEqualTo(ride2);
    }

    @Test
    void childTest() throws Exception {
        Ride ride = getRideRandomSampleGenerator();
        Child childBack = getChildRandomSampleGenerator();

        ride.setChild(childBack);
        assertThat(ride.getChild()).isEqualTo(childBack);

        ride.child(null);
        assertThat(ride.getChild()).isNull();
    }

    @Test
    void driverTest() throws Exception {
        Ride ride = getRideRandomSampleGenerator();
        Driver driverBack = getDriverRandomSampleGenerator();

        ride.setDriver(driverBack);
        assertThat(ride.getDriver()).isEqualTo(driverBack);

        ride.driver(null);
        assertThat(ride.getDriver()).isNull();
    }
}

package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.DriverTestSamples.*;
import static com.mycompany.myapp.domain.RideTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DriverTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Driver.class);
        Driver driver1 = getDriverSample1();
        Driver driver2 = new Driver();
        assertThat(driver1).isNotEqualTo(driver2);

        driver2.setId(driver1.getId());
        assertThat(driver1).isEqualTo(driver2);

        driver2 = getDriverSample2();
        assertThat(driver1).isNotEqualTo(driver2);
    }

    @Test
    void rideTest() throws Exception {
        Driver driver = getDriverRandomSampleGenerator();
        Ride rideBack = getRideRandomSampleGenerator();

        driver.addRide(rideBack);
        assertThat(driver.getRides()).containsOnly(rideBack);
        assertThat(rideBack.getDriver()).isEqualTo(driver);

        driver.removeRide(rideBack);
        assertThat(driver.getRides()).doesNotContain(rideBack);
        assertThat(rideBack.getDriver()).isNull();

        driver.rides(new HashSet<>(Set.of(rideBack)));
        assertThat(driver.getRides()).containsOnly(rideBack);
        assertThat(rideBack.getDriver()).isEqualTo(driver);

        driver.setRides(new HashSet<>());
        assertThat(driver.getRides()).doesNotContain(rideBack);
        assertThat(rideBack.getDriver()).isNull();
    }
}

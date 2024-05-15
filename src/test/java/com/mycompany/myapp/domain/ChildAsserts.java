package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class ChildAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertChildAllPropertiesEquals(Child expected, Child actual) {
        assertChildAutoGeneratedPropertiesEquals(expected, actual);
        assertChildAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertChildAllUpdatablePropertiesEquals(Child expected, Child actual) {
        assertChildUpdatableFieldsEquals(expected, actual);
        assertChildUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertChildAutoGeneratedPropertiesEquals(Child expected, Child actual) {
        assertThat(expected)
            .as("Verify Child auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertChildUpdatableFieldsEquals(Child expected, Child actual) {
        assertThat(expected)
            .as("Verify Child relevant properties")
            .satisfies(e -> assertThat(e.getName()).as("check name").isEqualTo(actual.getName()))
            .satisfies(e -> assertThat(e.getAge()).as("check age").isEqualTo(actual.getAge()))
            .satisfies(e -> assertThat(e.getSchoolName()).as("check schoolName").isEqualTo(actual.getSchoolName()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertChildUpdatableRelationshipsEquals(Child expected, Child actual) {
        assertThat(expected)
            .as("Verify Child relationships")
            .satisfies(e -> assertThat(e.getParent()).as("check parent").isEqualTo(actual.getParent()));
    }
}

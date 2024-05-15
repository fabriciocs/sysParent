package com.mycompany.myapp.service.mapper;

import static com.mycompany.myapp.domain.ChildAsserts.*;
import static com.mycompany.myapp.domain.ChildTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChildMapperTest {

    private ChildMapper childMapper;

    @BeforeEach
    void setUp() {
        childMapper = new ChildMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getChildSample1();
        var actual = childMapper.toEntity(childMapper.toDto(expected));
        assertChildAllPropertiesEquals(expected, actual);
    }
}

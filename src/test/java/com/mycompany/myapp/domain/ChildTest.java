package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.ChildTestSamples.*;
import static com.mycompany.myapp.domain.ParentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChildTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Child.class);
        Child child1 = getChildSample1();
        Child child2 = new Child();
        assertThat(child1).isNotEqualTo(child2);

        child2.setId(child1.getId());
        assertThat(child1).isEqualTo(child2);

        child2 = getChildSample2();
        assertThat(child1).isNotEqualTo(child2);
    }

    @Test
    void parentTest() throws Exception {
        Child child = getChildRandomSampleGenerator();
        Parent parentBack = getParentRandomSampleGenerator();

        child.setParent(parentBack);
        assertThat(child.getParent()).isEqualTo(parentBack);

        child.parent(null);
        assertThat(child.getParent()).isNull();
    }
}

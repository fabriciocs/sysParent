package com.mycompany.myapp.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ChildDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ChildDTO.class);
        ChildDTO childDTO1 = new ChildDTO();
        childDTO1.setId(1L);
        ChildDTO childDTO2 = new ChildDTO();
        assertThat(childDTO1).isNotEqualTo(childDTO2);
        childDTO2.setId(childDTO1.getId());
        assertThat(childDTO1).isEqualTo(childDTO2);
        childDTO2.setId(2L);
        assertThat(childDTO1).isNotEqualTo(childDTO2);
        childDTO1.setId(null);
        assertThat(childDTO1).isNotEqualTo(childDTO2);
    }
}

package com.edm.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successUsesZeroCodeAndSuccessMessage() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.code()).isZero();
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
    }

    @Test
    void pageResponseKeepsPaginationMetadata() {
        Page<String> page = new Page<>(2, 20);
        page.setTotal(81);
        page.setRecords(java.util.List.of("a", "b"));

        PageResponse<String> response = PageResponse.of(page);

        assertThat(response.records()).containsExactly("a", "b");
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.total()).isEqualTo(81);
        assertThat(response.totalPages()).isEqualTo(5);
    }
}

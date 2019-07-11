package clustercode.api.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class JobDocumentTest {

    private JobDocument subject = new JobDocument();

    @Test
    void getProgress_ShouldReturn_0_IfNoSlices() {
        assertThat(subject.getProgress()).isZero();
    }

    @Test
    void getProgress_ShouldReturn_0_IfNoSlicesComplete() {
        subject.setSlices(Arrays.asList(
                Slice.builder()
                        .nr(1)
                        .progress(0)
                        .build(),
                Slice.builder()
                        .nr(2)
                        .progress(0)
                        .build()
        ));
        assertThat(subject.getProgress()).isZero();
    }

    @Test
    void getProgress_ShouldReturn_100_IfAllCompleted() {
        subject.setSlices(Arrays.asList(
                Slice.builder()
                        .nr(1)
                        .progress(100)
                        .build(),
                Slice.builder()
                        .nr(2)
                        .progress(100)
                        .build()
        ));
        assertThat(subject.getProgress()).isEqualTo(100D);
    }

    @Test
    void getProgress_ShouldReturn_66_IfMostCompleted() {
        subject.setSlices(Arrays.asList(
                Slice.builder()
                        .nr(1)
                        .progress(100)
                        .build(),
                Slice.builder()
                        .nr(2)
                        .progress(100)
                        .build(),
                Slice.builder()
                        .nr(3)
                        .progress(0)
                        .build()
        ));
        assertThat(subject.getProgress()).isEqualTo(66.67D);
    }
}

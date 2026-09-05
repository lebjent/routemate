package com.trip.routemate.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Optional;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageStorageServiceTest {
    @TempDir Path root;
    StoredImageRepository repository;
    ImageStorageService storage;

    @BeforeEach void setup() {
        repository = mock(StoredImageRepository.class);
        storage = new ImageStorageService(new LocalStorageProperties(root.toString(), "optionProduct", "travelPlan", "/uploads/optionProduct", "/uploads/travelPlan"), repository);
    }

    @Test void storesActualImageInCategoryAndLoadsThroughDatabase() throws Exception {
        var uploaded = storage.store(ImageCategory.PRODUCT_DETAIL, png());
        var captor = org.mockito.ArgumentCaptor.forClass(StoredImage.class);
        verify(repository).saveAndFlush(captor.capture());
        var record = captor.getValue();
        assertThat(record.getRelativePath()).startsWith("optionProductDetail/").endsWith(".png");
        assertThat(uploaded.imageUrl()).isEqualTo("/api/public/images/" + record.getImageId());
        when(repository.findById(uploaded.imageId())).thenReturn(Optional.of(record));
        var loaded = storage.load(uploaded.imageId());
        assertThat(loaded.contentType()).isEqualTo("image/png");
        assertThat(loaded.resource().getContentAsByteArray()).isEqualTo(png().getBytes());
    }

    @Test void rejectsDisguisedHtmlAndEmptyFiles() {
        assertThatThrownBy(() -> storage.store(ImageCategory.DESTINATION, new MockMultipartFile("file", "x.png", "image/png", "<script>alert(1)</script>".getBytes())))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        assertThatThrownBy(() -> storage.store(ImageCategory.DESTINATION, new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("400");
        verifyNoInteractions(repository);
    }

    @Test void rejectsOversizedUpload() {
        assertThatThrownBy(() -> storage.store(ImageCategory.TRAVEL_PLAN, new MockMultipartFile("file", new byte[10 * 1024 * 1024 + 1])))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("413");
        verifyNoInteractions(repository);
    }

    @Test void removesFileIfDatabaseSaveFails() throws Exception {
        when(repository.saveAndFlush(any())).thenThrow(new IllegalStateException("database unavailable"));
        assertThatThrownBy(() -> storage.store(ImageCategory.RECOMMENDATION, png())).isInstanceOf(IllegalStateException.class);
        try (var paths = Files.walk(root)) { assertThat(paths.filter(Files::isRegularFile)).isEmpty(); }
    }

    @Test void rejectsPathsOutsideRootAndMissingFiles() {
        when(repository.findById("outside")).thenReturn(Optional.of(new StoredImage("outside", ImageCategory.PRODUCT, "../secret.png", "image/png", 1)));
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> storage.load("outside")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
        assertThatThrownBy(() -> storage.load("missing")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("404");
    }

    private MockMultipartFile png() throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        // 파일명과 Content-Type을 신뢰하지 않고 실제 PNG 내용으로 판별해야 한다.
        return new MockMultipartFile("file", "../../unsafe.jpg", "text/plain", bytes.toByteArray());
    }
}

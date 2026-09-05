package com.trip.routemate.common.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImageApiTest {
    static final Path ROOT;
    static { try { ROOT = Files.createTempDirectory("routemate-image-api-"); } catch (Exception e) { throw new ExceptionInInitializerError(e); } }
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) { registry.add("app.storage.root-directory", ROOT::toString); }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired StoredImageRepository images;

    @Test @WithMockUser(authorities = "DESTINATION_MANAGE")
    void uploadPersistsPathAndPublicEndpointReturnsImage() throws Exception {
        var file = png();
        var result = mvc.perform(multipart("/api/admin/images/DESTINATION").file(file)).andExpect(status().isOk()).andReturn();
        var json = mapper.readTree(result.getResponse().getContentAsString());
        var record = images.findById(json.get("imageId").asText()).orElseThrow();
        assertThat(Files.readAllBytes(ROOT.resolve(record.getRelativePath()))).isEqualTo(file.getBytes());
        mvc.perform(get(json.get("imageUrl").asText()).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png")).andExpect(content().bytes(file.getBytes()));
    }

    @Test @WithMockUser
    void normalUserCanUploadPlanButCannotUploadAdminImage() throws Exception {
        mvc.perform(multipart("/api/admin/images/DESTINATION").file(png())).andExpect(status().isForbidden());
        mvc.perform(multipart("/api/images/travel-plan").file(png())).andExpect(status().isOk());
    }

    @Test void anonymousCannotUploadAndMissingImageIs404() throws Exception {
        mvc.perform(multipart("/api/images/travel-plan").file(png())).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/public/images/missing")).andExpect(status().isNotFound());
    }

    private MockMultipartFile png() throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        return new MockMultipartFile("file", "test.png", "image/png", output.toByteArray());
    }
}

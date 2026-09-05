package com.trip.routemate.common.storage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.time.LocalDateTime;

/** 실제 파일의 위치와 형식을 보관한다. 경로는 NAS 루트에 대한 상대 경로다. */
@Entity
@Table(name = "TB_STORED_IMAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredImage {
    @Id @Column(name = "IMAGE_ID", length = 36) private String imageId;
    @Enumerated(EnumType.STRING) @Column(name = "CATEGORY", nullable = false, length = 30) private ImageCategory category;
    @Column(name = "RELATIVE_PATH", nullable = false, unique = true, length = 500) private String relativePath;
    @Column(name = "CONTENT_TYPE", nullable = false, length = 50) private String contentType;
    @Column(name = "FILE_SIZE", nullable = false) private long fileSize;
    @Column(name = "CREATE_DT", nullable = false) private LocalDateTime createDt;

    public StoredImage(String id, ImageCategory category, String path, String type, long size) {
        this.imageId = id;
        this.category = category;
        this.relativePath = path;
        this.contentType = type;
        this.fileSize = size;
        this.createDt = LocalDateTime.now();
    }
}

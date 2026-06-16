package com.lab.management.service.impl;

import com.lab.management.config.properties.MinioProperties;
import com.lab.management.config.properties.StorageProperties;
import com.lab.management.exception.BusinessException;
import com.lab.management.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final StorageProperties storageProperties;
    private final MinioProperties minioProperties;

    public FileStorageServiceImpl(StorageProperties storageProperties, MinioProperties minioProperties) {
        this.storageProperties = storageProperties;
        this.minioProperties = minioProperties;
    }

    @Override
    public Map<String, Object> store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        if (minioEnabled()) {
            try {
                return storeToMinio(file);
            } catch (Exception ignored) {
                return storeToLocal(file);
            }
        }
        return storeToLocal(file);
    }

    @Override
    public List<Map<String, Object>> listFiles() {
        if (minioEnabled()) {
            try {
                return listMinioFiles();
            } catch (Exception ignored) {
                return listLocalFiles();
            }
        }
        return listLocalFiles();
    }

    private Map<String, Object> storeToMinio(MultipartFile file) throws Exception {
        MinioClient client = minioClient();
        ensureBucket(client);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String savedName = UUID.randomUUID() + "_" + originalFilename;
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(savedName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("originalName", originalFilename);
        result.put("savedName", savedName);
        result.put("size", file.getSize());
        result.put("path", minioProperties.getBucket() + "/" + savedName);
        result.put("uploadedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("storage", "minio");
        return result;
    }

    private List<Map<String, Object>> listMinioFiles() throws Exception {
        MinioClient client = minioClient();
        ensureBucket(client);

        List<Map<String, Object>> files = new ArrayList<>();
        Iterable<Result<Item>> results = client.listObjects(
                ListObjectsArgs.builder().bucket(minioProperties.getBucket()).build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            Map<String, Object> file = new HashMap<>();
            file.put("name", item.objectName());
            file.put("size", item.size());
            file.put("path", minioProperties.getBucket() + "/" + item.objectName());
            file.put("updatedAt", item.lastModified() == null ? "-" :
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            .format(item.lastModified().toLocalDateTime()));
            file.put("storage", "minio");
            files.add(file);
        }

        files.sort(Comparator.comparing(item -> String.valueOf(item.get("updatedAt")), Comparator.reverseOrder()));
        return files;
    }

    private Map<String, Object> storeToLocal(MultipartFile file) {
        try {
            Path uploadDir = resolveUploadDir();
            Files.createDirectories(uploadDir);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
            String savedName = UUID.randomUUID() + "_" + originalFilename;
            Path target = uploadDir.resolve(savedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> result = new HashMap<>();
            result.put("originalName", originalFilename);
            result.put("savedName", savedName);
            result.put("size", file.getSize());
            result.put("path", target.toAbsolutePath().toString());
            result.put("uploadedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("storage", "local");
            return result;
        } catch (IOException ex) {
            throw new BusinessException("文件上传失败");
        }
    }

    private List<Map<String, Object>> listLocalFiles() {
        try {
            Path uploadDir = resolveUploadDir();
            if (!Files.exists(uploadDir)) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> files = new ArrayList<>();
            Files.list(uploadDir)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.toFile().lastModified(), Comparator.reverseOrder()))
                    .forEach(path -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", path.getFileName().toString());
                        item.put("size", path.toFile().length());
                        item.put("path", path.toAbsolutePath().toString());
                        item.put("storage", "local");
                        item.put("updatedAt", LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(path.toFile().lastModified()),
                                java.time.ZoneId.systemDefault()
                        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        files.add(item);
                    });
            return files;
        } catch (IOException ex) {
            throw new BusinessException("获取文件列表失败");
        }
    }

    private Path resolveUploadDir() {
        String configured = storageProperties.getUploadDir();
        String uploadDir = (configured == null || configured.trim().isEmpty()) ? "uploads" : configured.trim();
        return Paths.get(uploadDir);
    }

    private boolean minioEnabled() {
        return notBlank(minioProperties.getEndpoint())
                && notBlank(minioProperties.getAccessKey())
                && notBlank(minioProperties.getSecretKey())
                && notBlank(minioProperties.getBucket());
    }

    private MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    private void ensureBucket(MinioClient client) throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

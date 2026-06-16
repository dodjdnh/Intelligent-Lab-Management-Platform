package com.lab.management.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileStorageService {

    Map<String, Object> store(MultipartFile file);

    List<Map<String, Object>> listFiles();
}

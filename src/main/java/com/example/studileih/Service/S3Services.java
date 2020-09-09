package com.example.studileih.Service;

import java.io.ByteArrayOutputStream;

import org.springframework.web.multipart.MultipartFile;

public interface S3Services {
    public ByteArrayOutputStream downloadFile(String keyName);
    public void uploadFile(String keyName, MultipartFile file);
}
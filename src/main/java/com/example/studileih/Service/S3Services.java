package com.example.studileih.Service;

import java.io.ByteArrayOutputStream;

import com.amazonaws.services.s3.model.ObjectListing;
import org.springframework.web.multipart.MultipartFile;

public interface S3Services {
    public ByteArrayOutputStream downloadFile(String keyName);
    public void uploadFile(String keyName, MultipartFile file);
    public void copyFile(String fromKeyName, String toKeyName);
    public ObjectListing listObjects();
    public void deleteFile(String keyName);
//    public void deleteFiles(String keyName);
}
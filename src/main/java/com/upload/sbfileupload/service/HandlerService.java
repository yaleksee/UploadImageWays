package com.upload.sbfileupload.service;

import com.upload.sbfileupload.commons.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.net.URL;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 */
public interface HandlerService {
    MultipartFile createResizedCopy(
            Image originalImage,
            int scaledWidth,
            int scaledHeight,
            boolean preserveAlpha
    );

    MultipartFile urlImageToMultipartFile(Image image);

    MultipartFile jsonBase64ImageToMultipartFile(String formWrapper);

    Image createImage(URL requestURL);

    Image createImage(String requestJsonString);

    Image createImage(MultipartFile multipartFile);

    FileResponse uploadFile(MultipartFile file, String fileRootLocationName);

}

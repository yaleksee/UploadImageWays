package com.upload.sbfileupload.service;

import com.upload.sbfileupload.commons.FileResponse;
import com.upload.sbfileupload.handler.ImageHandler;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 * <p>
 * Сервис для обработки загруженных файлов.
 * 1. Извлечение изобаржений из источника,
 * 2. Валидация
 * 3. Создание миниатюры.
 */
@Service
public class MultipartFileHandleService implements HandlerService {
    private static final Logger log = Logger.getLogger(MultipartFileHandleService.class);

    private ImageHandler imageHandler;

    public MultipartFileHandleService() {
        this.imageHandler = new ImageHandler();
    }

    /**
     * Создание миниатюры
     *
     * @param originalImage - исходник изображения
     * @param scaledWidth   - заданная ширина
     * @param scaledHeight  - заданная высота
     * @param preserveAlpha - логика сжатия изображения в библиотеке java.awt.
     *                      TRUE - убыстряет процесс но уудшает качество
     * @return - миниатура в формате MultipartFile
     */
    @Override
    public MultipartFile createResizedCopy(
            Image originalImage,
            int scaledWidth,
            int scaledHeight,
            boolean preserveAlpha
    ) {
        MultipartFile multipartFile = imageHandler.createResizedCopy(originalImage, scaledWidth, scaledHeight, preserveAlpha);
        return multipartFile;
    }

    /**
     * Получение MultipartFile из image
     *
     * @param image
     * @return
     */
    @Override
    public MultipartFile urlImageToMultipartFile(Image image) {
        BufferedImage bufferedImage = imageHandler.toBufferedImage(image);
        MultipartFile multipartFile = imageHandler.getMultipartFile(bufferedImage);
        return multipartFile;
    }

    /**
     * Получение MultipartFile из строки с BASE64 закодированным изображением
     *
     * @param formWrapper
     * @return
     */
    @Override
    public MultipartFile jsonBase64ImageToMultipartFile(String formWrapper) {
        BufferedImage bufferedImage = imageHandler.decodeToImage(formWrapper);
        MultipartFile multipartFile = imageHandler.getMultipartFile(bufferedImage);
        return multipartFile;
    }

    /**
     * Получение изображения из сети
     *
     * @param requestURL
     * @return
     */
    @Override
    public Image createImage(URL requestURL) {
        Image image = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(requestURL);
            return image = imageHandler.validation(bufferedImage);
        } catch (Throwable e) {
            log.error(e.getMessage());
            log.info("\n Не удалось распознать файл как изображение");
            return image;
        }
    }

    /**
     * Получение изображения из строки с BASE64 закодированным изображением
     *
     * @param requestJsonString
     * @return
     */
    @Override
    public Image createImage(String requestJsonString) {
        Image image = null;
        try {
            BufferedImage bufferedImage = imageHandler.decodeToImage(requestJsonString);
            if (bufferedImage == null) {
                log.info("\n Не удалось распознать файл как изображение");
                return null;
            }
            return image = imageHandler.validation(bufferedImage);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.info("\n Не удалось распознать файл как изображение");
            return image;
        }
    }

    /**
     * Получение изображения из ПК
     *
     * @param multipartFile
     * @return
     */
    @Override
    public Image createImage(MultipartFile multipartFile) {
        Image image = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
            if (bufferedImage == null) {
                log.info("\n Не удалось распознать файл как изображение");
                return null;
            }
            return image = imageHandler.validation(bufferedImage);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.info("\n Не удалось распознать файл как изображение");
            return image;
        }
    }

    /**
     * Сохранение изображения в формате MultipartFile
     *
     * @param file                 - файл для сохранения
     * @param fileRootLocationName - путь сохранения
     * @return - ответ клиенту
     */
    @Override
    public FileResponse uploadFile(MultipartFile file, String fileRootLocationName) {
        if (file != null && !file.isEmpty()) {
            String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/download/")
                    .path(fileRootLocationName)
                    .toUriString();

            return new FileResponse(fileRootLocationName, uri, file.getContentType(), file.getSize());
        }
        return null;
    }

}

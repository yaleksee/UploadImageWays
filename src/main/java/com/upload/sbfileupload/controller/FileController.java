package com.upload.sbfileupload.controller;

import com.upload.sbfileupload.commons.FileResponse;
import com.upload.sbfileupload.commons.FormWrapper;
import com.upload.sbfileupload.service.HandlerService;
import com.upload.sbfileupload.service.StorageService;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 * Основной контроллер приложения.
 * Принимает файлы с изображениями и отправляет их на обработку и валидацию
 */
@Controller
public class FileController {
    private static final Logger log = Logger.getLogger(FileController.class);
    private StorageService storageService;
    private HandlerService handlerService;

    public FileController(StorageService storageService, HandlerService handlerService) {
        this.storageService = storageService;
        this.handlerService = handlerService;
    }

    /**
     * Метод возвращает список уже обработанных файлов в модели
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String listAllFiles(Model model) {
        log.info("\n Был вызван REST метод \"/\"");
        model.addAttribute("files", storageService.loadAll().map(
                path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/download/")
                        .path(path.getFileName().toString())
                        .toUriString())
                .collect(Collectors.toList()));

        return "listFiles";
    }

    /**
     * Метод позволяет загрузить файл из view при нажатии ссылки
     *
     * @param filename - имя сохраненного файла в системе
     * @return
     */
    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        log.info("\n Был загружен файл + " + filename);
        Resource resource = storageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Метод принимает на вход изображения 3 способами
     * 1. Через URL
     * 2. Мультизагрузкой нескольких изображений
     * 3. Загрузка нескольких изображений в формате BASE 64 через JSON
     * <p>
     * Реализована поддержка загрузки как одного так и нескольких изображений сразу
     *
     * @param requestURL  - адрес изображения в сети
     * @param files       - файлы с ПК
     * @param formWrapper - JSON запрос с зашитыми изображениями
     * @return
     */
    @PostMapping(value = "/upload-multiple-files")
    @ResponseBody
    public List<FileResponse> uploadMultipleFiles(

            @RequestParam(value = "urls", required = false) @NotEmpty URL[] requestURL,
            @RequestParam(value = "files", required = false) @NotEmpty MultipartFile[] files,
            @RequestBody(required = false) @NotBlank String formWrapper

    ) {

        /**
         * Подготовили коллекции для хранения:
         * imagesForResize - успешно обработанных изображений
         * fileResponses - отеветов клиенту
         */

        List<FileResponse> fileResponses = new ArrayList<>();
        List<Image> imagesForResize = new ArrayList<>();

        /**
         * Обработка изображений переданных в URL
         */

        if (requestURL != null) {
            for (URL singleRequestURL : requestURL) {
                if (singleRequestURL != null) {
                    log.info("\n Начата обработка файла по URL");
                    Image image = handlerService.createImage(singleRequestURL);
                    if (image == null) {
                        continue;
                    }
                    MultipartFile multipartFile = handlerService.urlImageToMultipartFile(image);
                    String fileRootLocationName = storageService.store(multipartFile);
                    FileResponse fileResponseURL = handlerService.uploadFile(multipartFile, fileRootLocationName);

                    log.info("\n Был обработан файл + " + fileResponseURL.getName());
                    fileResponses.add(fileResponseURL);
                    imagesForResize.add(image);
                }
            }
        }

        /**
         * Обработка изображений переданных с ПК
         */

        if (files != null) {
            for (MultipartFile multipartFile : files) {
                if (multipartFile != null) {
                    log.info("\n Начата обработка файла с ПК");
                    Image image = handlerService.createImage(multipartFile);
                    if (image == null) {
                        continue;
                    }
                    String fileRootLocationName = storageService.store(multipartFile);
                    FileResponse fileResponseArray = handlerService.uploadFile(multipartFile, fileRootLocationName);

                    log.info("\n Был обработан файл + " + fileResponseArray.getName());
                    fileResponses.add(fileResponseArray);
                    imagesForResize.add(image);
                }
            }
        }

        /**
         * Обработка изображений переданных в JSON
         */

        if (formWrapper != null) {
            if (formWrapper.startsWith("{\"formWrapper\"")) {
                Gson g = new Gson();
                FormWrapper fW = g.fromJson(formWrapper, FormWrapper.class);

                for (String requestJsonString : fW.getPayload()) {
                    log.info("\n Начата обработка файла из JSON");
                    Image image = handlerService.createImage(requestJsonString);
                    if (image == null) {
                        continue;
                    }
                    MultipartFile multipartFile = handlerService.jsonBase64ImageToMultipartFile(requestJsonString);
                    String fileRootLocationName = storageService.store(multipartFile);
                    FileResponse fileResponseJSON = handlerService.uploadFile(multipartFile, fileRootLocationName);

                    log.info("\n Был обработан файл + " + fileResponseJSON.getName());
                    fileResponses.add(fileResponseJSON);
                    imagesForResize.add(image);
                }
            }
        }

        /**
         * Создание миниатюр 100*100 для всех успешно загруженных изображений.
         *
         * Миниатюры хранятся вместе с загруженными изображениями,
         * но не кладуться в fileResponses для вывода клиенту fileResponses
         */

        for (Image image : imagesForResize) {
            MultipartFile multipartFile
                    = handlerService.createResizedCopy(image, 100, 100, true);
            String fileRootLocationName = storageService.store(multipartFile);
            FileResponse fileResizedCopy = handlerService.uploadFile(multipartFile, fileRootLocationName);
            log.info("\n Была создана миниатюра + " + fileResizedCopy.getName());
        }

        return fileResponses;
    }

    /**
     * Удаление всех файлов
     *
     * @return
     */
    @GetMapping("/deleteAll")
    public String delete() {

        storageService.deleteAll();
        log.info("\n Все обработанные файлы удалены");
        return "listFiles";
    }
}

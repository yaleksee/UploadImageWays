package com.upload.sbfileupload.service;

import com.upload.sbfileupload.config.StorageProperties;
import com.upload.sbfileupload.exeptions.FileNotFoundException;
import com.upload.sbfileupload.exeptions.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 * <p>
 * Сервис для операций с уже обработанными и валидированными изображениями
 */
@Service
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Сохранение файла в системе
     *
     * @param file - файл для сохранения
     * @return - уникальное имя сохраненного файла
     */
    @Override
    public String store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Не удалось сохранить пустой файл " + filename);
            }
            if (filename.contains(".....")) {
                // Дополнительная валидация
                throw new StorageException(
                        "Не удается сохранить файл с относительным путем вне текущего каталога " + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Не удалось сохранить файл " + filename, e);
        }

        return filename;
    }

    /**
     * Выгрузка изображений
     *
     * @return
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Не удалось прочитать сохраненные файлы\n", e);
        }

    }

    /**
     * Загрузка изображения
     *
     * @param filename
     * @return
     */
    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * Путь хранения изображения в системе
     *
     * @param filename
     * @return
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException(
                        "Не удалось прочитать файл: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Не удалось прочитать файл: " + filename, e);
        }
    }

    /**
     * Удаления всех изображений
     */
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}

package com.upload.sbfileupload.exeptions;


import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends StorageException {
    private static final Logger log = Logger.getLogger(FileNotFoundException.class);

    public FileNotFoundException(String message) {
        super(message);
        log.error(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
        log.error(message);
    }
}

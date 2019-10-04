package com.upload.sbfileupload.exeptions;


import org.apache.log4j.Logger;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 */
public class StorageException extends RuntimeException {
    private static final Logger log = Logger.getLogger(StorageException.class);

    public StorageException(String message) {
        super(message);
        log.error(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        log.error(message);
    }
}

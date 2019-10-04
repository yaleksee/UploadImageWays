package com.upload.sbfileupload.commons;

import java.io.Serializable;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 * Объект для вывода информации о сохраненном изображении
 * name - имя файла
 * uri - путь хранения
 * type - тип файла
 * size - размер файла
 */
public class FileResponse implements Serializable {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private String name;
    private String uri;
    private String type;
    private long size;

    public FileResponse(String name, String uri, String type, long size) {
        this.name = name;
        this.uri = uri;
        this.type = type;
        this.size = size;
    }

    public FileResponse() {
    }
}

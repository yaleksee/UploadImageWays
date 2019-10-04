package com.upload.sbfileupload.commons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alekseenkoyuri1989@gmail.com on 01.10.2019
 * Объект для фиксации входящего JSON с зашитыми изображениями в формате BASE64
 */
public class FormWrapper implements Serializable {
    public FormWrapper(List<String> formWrapper) {
        this.formWrapper = formWrapper;
    }

    public FormWrapper() {
    }

    @JsonProperty("formWrapper")
    public List<String> getPayload() {
        return formWrapper;
    }

    @JsonProperty("formWrapper")
    public void setPayload(List<String> payload) {
        this.formWrapper = payload;
    }


    @JsonProperty("formWrapper")
    private List<String> formWrapper = new ArrayList<>();
}

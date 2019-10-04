package com.upload.sbfileupload;

import com.google.gson.Gson;
import com.upload.sbfileupload.commons.FileResponse;
import com.upload.sbfileupload.commons.FormWrapper;
import com.upload.sbfileupload.service.HandlerService;
import com.upload.sbfileupload.service.StorageService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootFileUploadApplicationTests {
    private FileResponse fileResponse;

    private BufferedReader brJson;
    private BufferedReader brResponseTestJson;
    private BufferedReader brJsonWrong;
    private FileInputStream inputImgPC;
    private FileInputStream inputImgPCWrong;

    @Autowired
    private HandlerService handlerService;
    @Autowired
    private StorageService storageService;
    private URL singleRequestURL = null;
    private URL singleRequestURLWrong = null;

    public SpringBootFileUploadApplicationTests() throws MalformedURLException {
        singleRequestURL = new URL("https://s1.1zoom.ru/big0/344/292505-Sepik.jpg");
        singleRequestURLWrong = new URL("https://www.eldorado.ru/instructions/71345349.pdf");
    }

    @Before
    public void contextLoads() throws IOException {
        fileResponse = new FileResponse("name", "path", "jpg", 100);
        ClassLoader classLoader = getClass().getClassLoader();
        File fileJson = new File(classLoader.getResource("FormWrapper.json").getFile());
        File ResponseTest = new File(classLoader.getResource("ResponseTest.json").getFile());
        File fileJsonWrong = new File(classLoader.getResource("FormWrapperWrong.json").getFile());
        File fileImage = new File(classLoader.getResource("TEST.jpg").getFile());
        File fileImageWrong = new File(classLoader.getResource("ForTest.pdf").getFile());
        FileReader fileReaderJson = new FileReader(fileJson);
        FileReader fileResponseTest = new FileReader(ResponseTest);
        FileReader fileReaderJsonWrong = new FileReader(fileJsonWrong);
        inputImgPC = new FileInputStream(fileImage);
        inputImgPCWrong = new FileInputStream(fileImageWrong);
        brJson = new BufferedReader(fileReaderJson);
        brResponseTestJson = new BufferedReader(fileResponseTest);
        brJsonWrong = new BufferedReader(fileReaderJsonWrong);
    }

    /**
     * Тест проверяет что если мы будет передавать один и тод же файл являющимся изображением,
     * то
     * 1. Мы точно сможем заполнить объект для ответа Клиенту.
     * 2. Этот ответ будет совпадать для одного и того же файла с полученным сервисом заранее.
     * 3. Данные полученные в тесте будут совпадать с ожидаемыми
     * (тип изображения, его размер, путь хранения и имя)
     * @throws IOException
     */
    @Test
    public void whenHASImage_thenCreateFileResponseJSON() throws IOException {

        MultipartFile multipartFile = new MockMultipartFile("file",
                "TEST.jpg", "image/jpg", IOUtils.toByteArray(inputImgPC));

        String fileRootLocationName = storageService.store(multipartFile);
        FileResponse fileCopy = handlerService.uploadFile(multipartFile, fileRootLocationName);
        FileResponse fileResponse = new Gson().fromJson(brResponseTestJson, FileResponse.class);
        assertEquals(fileCopy.getType(), fileResponse.getType());
        assertEquals(fileCopy.getName(), fileResponse.getName());
        assertEquals(fileCopy.getUri(), fileResponse.getUri());
        assertEquals(fileCopy.getSize(), fileResponse.getSize());
    }

    /**
     * Тест проверяет что если мы будет передавать файл являющимся изображением по URL,
     * то
     * 1. Мы точно сможем заполнить объект для ответа Клиенту.
     * 2. Данные в этом ответе будут совпадать с ожидаемыми
     * (тип изображения и его размер), а имя не будет пустым.
     *
     */
    @Test
    public void whenRequestURLHASImage_thenCreateFileResponse() {

        Image image = handlerService.createImage(singleRequestURL);
        MultipartFile multipartFile = handlerService.urlImageToMultipartFile(image);
        String fileRootLocationName = storageService.store(multipartFile);
        FileResponse fileCopy = handlerService.uploadFile(multipartFile, fileRootLocationName);
        assertNotNull(fileCopy);
        assertEquals(multipartFile.getContentType(), "image/jpg");
        assertEquals(multipartFile.getSize(), 93138);
        assertEquals(!fileRootLocationName.isEmpty(), true);
    }

    /**
     * Тест проверяет что если мы будет передавать файл являющимся изображением закодированным в BASE64,
     * то
     * 1. Мы точно сможем распарсить изображение, заполнить объект для ответа Клиенту.
     * 2. Данные в этом ответе будут совпадать с ожидаемыми
     * (тип изображения и его размер), а имя не будет пустым.
     *
     */
    @Test
    public void whenJSONHASImageBAse64_thenCreateFileResponse() {

        FormWrapper formWrapper = new Gson().fromJson(brJson, FormWrapper.class);
        String requestJsonString = formWrapper.getPayload().get(0);
        Image image = handlerService.createImage(requestJsonString);
        MultipartFile multipartFile = handlerService.jsonBase64ImageToMultipartFile(requestJsonString);
        String fileRootLocationName = storageService.store(multipartFile);
        FileResponse fileCopy = handlerService.uploadFile(multipartFile, fileRootLocationName);
        assertNotNull(fileCopy);
        assertEquals(multipartFile.getContentType(), "image/jpg");
        assertEquals(!fileRootLocationName.isEmpty(), true);
        assertEquals(image != null, true);
    }

    /**
     * Тест проверяет что если мы будет передавать файл являющимся изображением С ПК,
     * то
     * 1. Мы точно сможем заполнить объект для ответа Клиенту.
     * 2. Данные в этом ответе будут совпадать с ожидаемыми
     * (тип изображения и его размер), а имя не будет пустым.
     *
     */
    @Test
    public void whenMultipartFileHASImage_thenCreateFileResponse() throws IOException {

        MultipartFile multipartFile = new MockMultipartFile("file",
                "TEST.jpg", "image/jpg", IOUtils.toByteArray(inputImgPC));

        Image image = handlerService.createImage(multipartFile);
        String fileRootLocationName = storageService.store(multipartFile);
        FileResponse fileCopy = handlerService.uploadFile(multipartFile, fileRootLocationName);
        assertNotNull(fileCopy);
        assertEquals(multipartFile.getContentType(), "image/jpg");
        assertEquals(!fileRootLocationName.isEmpty(), true);
        assertEquals(image != null, true);
    }

    /**
     * Тест проверяет что если мы будет передавать файл НЕ являющимся изображением по URL,
     * то
     * 1. то система валидации вернем нам нулл в качестве изображения
     *
     */
    @Test
    public void whenRequestURLHASFileNotTypeImage_thenCreateImageIsNull() {
        Image image = handlerService.createImage(singleRequestURLWrong);
        assertEquals(image == null, true);
    }

    /**
     * Тест проверяет что если мы будет передавать BASE64 файл НЕ являющимся изображением в JSON,
     * то
     * 1. то система валидации вернем нам нулл в качестве изображения
     *
     */
    @Test
    public void whenMultipartFileHASFileNotTypeImage_thenCreateImageIsNull() throws IOException {
        MultipartFile multipartFile = new MockMultipartFile("file",
                "ForTest.pdf", "file/pdf", IOUtils.toByteArray(inputImgPCWrong));

        Image image = handlerService.createImage(multipartFile);
        assertEquals(image == null, true);
    }

    /**
     * Тест проверяет что если мы будет передавать файл НЕ являющимся изображением с ПК,
     * то
     * 1. то система валидации вернем нам нулл в качестве изображения
     *
     */
    @Test
    public void whenJSONHASFileNotTypeImageBAse64_thenCreateImageGetException() {
        FormWrapper formWrapper = new Gson().fromJson(brJsonWrong, FormWrapper.class);
        String requestJsonString = formWrapper.getPayload().get(0);

        Throwable thrown = assertThrows(Exception.class, () -> {
            handlerService.createImage(requestJsonString);
        });
        assertNotNull(thrown, thrown.getMessage());
    }

    /**
     * Тест проверяет что если мы будет обработали файл являющимся изображением полученным любым путем,
     * то
     * 1. то будет создана миниатура с параметрами соотвествующим ожидаемым.
     *
     */
    @Test
    public void whenCreatedFileResponse_thenCreateMiniatureOfImage() throws IOException {
        MultipartFile multipartFile = new MockMultipartFile("file",
                "TEST.jpg", "image/jpg", IOUtils.toByteArray(inputImgPC));

        Image image = handlerService.createImage(multipartFile);
        MultipartFile multipartFileResizedCopy = handlerService
                .createResizedCopy(image, 100, 100, true);
        String fileRootLocationName = storageService.store(multipartFileResizedCopy);
        FileResponse fileResizedCopy = handlerService.uploadFile(multipartFileResizedCopy, fileRootLocationName);
        assertNotNull(fileResizedCopy);
        assertEquals(fileResizedCopy.getType(), "image/jpg");
        assertEquals(fileResizedCopy.getSize(), 2033);
    }


}

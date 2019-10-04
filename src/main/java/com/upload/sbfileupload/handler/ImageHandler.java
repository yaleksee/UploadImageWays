package com.upload.sbfileupload.handler;

import net.bytebuddy.utility.RandomString;
import org.apache.log4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;

public class ImageHandler {
    private static final Logger log = Logger.getLogger(ImageHandler.class);

    public MultipartFile createResizedCopy(
            Image originalImage,
            int scaledWidth,
            int scaledHeight,
            boolean preserveAlpha
    ) {
        MultipartFile multipartFile = null;
        try {
            int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
            BufferedImage bufferedImage = new BufferedImage(scaledWidth, scaledHeight, imageType);
            Graphics2D g = bufferedImage.createGraphics();
            if (preserveAlpha) {
                g.setComposite(AlphaComposite.Src);
            }
            g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g.dispose();
            multipartFile = getMultipartFile(bufferedImage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return multipartFile;
    }


    /**
     * Декодирование изображения из строки в формат BufferedImage
     *
     * @param imageString
     * @return
     */
    public BufferedImage decodeToImage(String imageString) {
        byte[] imageByte = Base64.getDecoder().decode(imageString);
        BufferedImage bufferedImage = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);) {
            bufferedImage = ImageIO.read(bis);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return bufferedImage;
    }

    /**
     * Конвертация Image в BufferedImage
     *
     * @param img
     * @return
     */
    public BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    /**
     * Конвертация BufferedImage в MultipartFile
     *
     * @param bufferedImage
     * @return
     */
    public MultipartFile getMultipartFile(BufferedImage bufferedImage) {
        MultipartFile multipartFile = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            String fileName = RandomString.make() + new Date().getTime() + ".jpg";
            multipartFile = new MockMultipartFile(
                    fileName, fileName, "image/jpg", byteArrayOutputStream.toByteArray()
            );
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return multipartFile;
    }

    /**
     * Проверка что загруженный и уже обработанный файл является изображением в соотвествии с библиотекой java.awt
     *
     * @param bufferedImage
     * @return
     * @throws IOException
     */
    public Image validation(BufferedImage bufferedImage) throws IOException {
        Image image = null;
        try {
            File outputFile = new File("saved.png");
            ImageIO.write(bufferedImage, "png", outputFile);
            image = ImageIO.read(new File("saved.png"));
            return image;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}

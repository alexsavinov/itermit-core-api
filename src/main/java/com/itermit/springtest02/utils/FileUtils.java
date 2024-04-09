package com.itermit.springtest02.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@Getter
public class FileUtils {

    @Value("${app.publicPath}")
    private String publicPath;

    @Value("${app.avatarImagePath}")
    private String avatarImagePath;

    @Value("${app.articleImagePath}")
    private String articleImagePath;

    public Optional<String> getExtension(String filename) {
        return ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public String getChecksum(MultipartFile file) {
        byte[] hash;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            hash = messageDigest.digest(file.getBytes());
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Error while generating checksum. {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return new BigInteger(1, hash).toString(16);
    }

    public String getBaseFolder() {
        if (publicPath.startsWith("file://")) {
            return publicPath.replace("file://", "").replace("/", File.separator);
        } else {
            return System.getProperty("user.dir") + publicPath.replace("/", File.separator);
        }
    }

    public String saveToStaticFolder(MultipartFile multipartFile, String subFolder) {
        if (ofNullable(multipartFile).isEmpty()) {
            return "";
        }

        String checksum = getChecksum(multipartFile);
        String extension = getExtension(multipartFile.getOriginalFilename()).orElse("jpg");
        String fileName = checksum + "." + extension;

        String path = getBaseFolder() + subFolder + File.separator + fileName;
        File file = new File(path);

        if (!file.exists()) {
            try {
                multipartFile.transferTo(file);
            } catch (IOException e) {
                log.error("Error while saving file.");
                throw new RuntimeException(e);
            }
        }

        return fileName;
    }

    public String saveAvatar(MultipartFile multipartFile) {
        return saveToStaticFolder(multipartFile, getAvatarImagePath());
    }

    public String saveArticleImage(MultipartFile multipartFile) {
        return saveToStaticFolder(multipartFile, getArticleImagePath());
    }
}

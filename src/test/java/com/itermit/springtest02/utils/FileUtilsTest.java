package com.itermit.core.utils;

import com.itermit.core.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

    @Mock
    private MessageDigest messageDigest;
    @InjectMocks
    private FileUtils subject;

    @Test
    void getExtensionShouldReturnCorrectExtension() {
        String filename = "test.txt";
        Optional<String> extension = subject.getExtension(filename);
        assertEquals("txt", extension.orElse(null));
    }

    @Test
    void getChecksumShouldReturnCorrectChecksum() throws NoSuchAlgorithmException, IOException {
        byte[] bytes = "test data".getBytes();

        try (MockedStatic<MessageDigest> utilities = mockStatic(MessageDigest.class)) {
            utilities.when(() -> MessageDigest.getInstance(anyString())).thenReturn(messageDigest);

            when(messageDigest.digest(bytes)).thenReturn(new byte[]{1, 2, 3});

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "file", "test.txt", "text/plain", bytes);
            String checksum = subject.getChecksum(mockMultipartFile);

            assertEquals(new BigInteger(1, new byte[]{1, 2, 3}).toString(16), checksum);
        }
    }

    @Test
    void getChecksumShouldTrowNoSuchAlgorithmException() throws NoSuchAlgorithmException, IOException {
        byte[] bytes = "test data".getBytes();

        try (MockedStatic<MessageDigest> utilities = mockStatic(MessageDigest.class)) {
            utilities.when(() -> MessageDigest.getInstance(anyString())).thenThrow(new NoSuchAlgorithmException());

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "file", "test.txt", "text/plain", bytes);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> subject.getChecksum(mockMultipartFile));

            String expectedMessage = "java.security.NoSuchAlgorithmException";
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);

        }
    }

    @Test
    void getBaseFolderShouldReturnCorrectBaseFolder() {
        ReflectionTestUtils.setField(subject, "publicPath", "/src/");

        String baseFolder = subject.getBaseFolder();

        String expected = System.getProperty("user.dir") + subject.getPublicPath().replace("/", File.separator);
        assertEquals(expected, baseFolder);
    }

    @Test
    void saveToStaticFolder_ShouldThrowRuntimeException() throws IOException {
        ReflectionTestUtils.setField(subject, "publicPath", "/src/");

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test data".getBytes()
        );

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> subject.saveToStaticFolder(mockMultipartFile, "/testFolder"));

//        subject.saveToStaticFolder(mockMultipartFile, "/testFolder");
//        verify(mockMultipartFile, times(1)).transferTo(any(File.class));
    }

    @Test
    void saveToStaticFolder_ShouldReturnEmptyString() throws IOException {
        ReflectionTestUtils.setField(subject, "publicPath", "/src/");

        String fileName = subject.saveToStaticFolder(null, "/testFolder");

        assertThat(fileName).isEqualTo("");
    }

    @Test
    void saveAvatar_ShouldThrowRuntimeException() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test data".getBytes()
        );
        ReflectionTestUtils.setField(subject, "publicPath", "/src/");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> subject.saveAvatar(mockMultipartFile));
    }

    @Test
    void saveArticleImage_ShouldThrowRuntimeException() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test data".getBytes()
        );
        ReflectionTestUtils.setField(subject, "publicPath", "/src/");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> subject.saveArticleImage(mockMultipartFile));
    }

}
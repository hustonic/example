package io.github.hustonic.example.examplecodegenerator;

import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * @author Huston
 */
public class Test {
    public static void main(String[] args) throws IOException {
        File file = new File("D:\\download\\logback-v_1.2.3.zip");
//        encodeToBase64File(file);
        decodeFromBase64File(file);
    }

    private static void encodeToBase64File(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        byte[] encode = Base64.getEncoder().encode(bytes);
        FileCopyUtils.copy(encode, file);
    }

    private static void decodeFromBase64File(File file) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        byte[] decode = Base64.getDecoder().decode(bytes);
        FileCopyUtils.copy(decode, file);
    }
}

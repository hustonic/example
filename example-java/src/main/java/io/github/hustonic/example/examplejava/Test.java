package io.github.hustonic.example.examplejava;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * @author Huston
 */
public class Test {

    public static void main(String[] args) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            throw new CompletionException(new Exception("Error"));
        }).handleAsync((result, throwable) -> {
            System.out.println("result = " + result);
            System.out.println("throwable = " + throwable);
            return null;
        }).handleAsync((result, throwable) -> {
            System.out.println("result = " + result);
            System.out.println("throwable = " + throwable);
            return null;
        });
        future.join();
    }

}

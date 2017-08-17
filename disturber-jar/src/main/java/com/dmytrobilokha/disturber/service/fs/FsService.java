package com.dmytrobilokha.disturber.service.fs;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * The service responsible for operations with a file system
 */
@ApplicationScoped
public class FsService {

    public boolean pathExists(Path path) {
        return Files.exists(path);
    }

    public void copyResourceIfFileAbsent(Path file, String resource) throws IOException {
        if (pathExists(file))
            return;
        try (InputStream defaultPropertiesInputStream = getClass().getResourceAsStream(resource);
             BufferedInputStream defaultPropertiesBufferedInputStream = new BufferedInputStream(defaultPropertiesInputStream)){
            Files.copy(defaultPropertiesBufferedInputStream, file);
        }
    }

    public void consumeFile(Path file, ThrowingConsumer<Reader> consumer) throws Exception {
        try (Reader reader = Files.newBufferedReader(file)) {
            consumer.accept(reader);
        }
    }

    public <T> T readFile(Path file, ThrowingFunction<Reader, T> readingFunction) throws Exception {
        try (Reader reader = Files.newBufferedReader(file)) {
            return readingFunction.apply(reader);
        }
    }

    public void writeFile(Path file, ThrowingConsumer<Writer> consumer) throws Exception {
        try (Writer writer = Files.newBufferedWriter(file
                , StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            consumer.accept(writer);
        }
    }

}

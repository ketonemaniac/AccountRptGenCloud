package net.ketone.accrptgen.service.store;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static java.util.function.Predicate.not;

@Slf4j
public class FileStorageService implements StorageService {

    private String storageFolder;

    public FileStorageService(final String storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public String store(byte[] input, String filename) throws IOException {
        log.info("Writing to local file " + storageFolder + filename);
        File output= new File(storageFolder + filename);
        FileOutputStream out = new FileOutputStream(output);
        out.write(input);
        out.close();
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        try {
            return new FileInputStream(storageFolder + filename);
        } catch (FileNotFoundException e) {
            return new NullInputStream(0);
        }
    }

    @Override
    public byte[] load(String filename) {
        try {
            return Files.readAllBytes(new File(storageFolder + filename).toPath());
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public List<String> list() {
        return listDir(storageFolder)
                .filter(not(File::isDirectory))
                .map(tuple2 -> tuple2.getPath())
                .map(s -> s.substring(storageFolder.length()))
                .collectList()
                .block();
    }

    private Flux<File> listDir(final String dir) {
        return Mono.just(new File(dir))
                .map(File::listFiles)
                .flatMapIterable(Arrays::asList)
                .expand(f -> f.isDirectory() ?
                        listDir(f.getPath()) : Flux.empty());
    }

    @Override
    public void delete(String filename) {
        File f = new File(storageFolder + filename);
        if(f.exists()) {
            log.info("Deleting local file " + storageFolder + filename);
            boolean success = f.delete();
            log.info("Deleted=" + success);
        }
    }

    @Override
    public boolean hasFile(String filename) {
        File f = new File(storageFolder + filename);
        return f.exists();
    }

}

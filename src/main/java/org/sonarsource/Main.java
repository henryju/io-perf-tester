package org.sonarsource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class Main {

    private static boolean debug = false;

    public static void main(String[] args) throws IOException {
        Set<String> argsSet = Set.of(args);
        debug = argsSet.contains("--debug");

        Instant start = Instant.now();
        Path currentDir = Paths.get("").toAbsolutePath();

        AtomicLong filesCount = new AtomicLong();
        AtomicLong allLinesCount = new AtomicLong();
        Files.walkFileTree(currentDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path subDir, BasicFileAttributes attrs)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                if (exc instanceof AccessDeniedException) {
                    debugErr("Access to folder '" + file.toAbsolutePath().toString() + "' was denied, therefore skipping it.");
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                filesCount.incrementAndGet();
                try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
                    allLinesCount.addAndGet(stream.count());
                } catch (UncheckedIOException e) {
                    if (e.getCause() instanceof MalformedInputException) {
                        debugErr("Not a UTF-8 file: " + file + ": " + e.getMessage());
                    } else {
                        logWithStackTrace(e, file);
                    }
                } catch (Exception e) {
                    logWithStackTrace(e, file);
                }
                return super.visitFile(file, attrs);
            }
        });
        Instant end = Instant.now();

        long durationSec = Duration.between(start, end).get(ChronoUnit.SECONDS);
        System.out.println("Counted " + allLinesCount.get() + " lines on " + filesCount.get() + " files in " + durationSec + "s");
        System.out.println("Throughput: " + (allLinesCount.get() / durationSec) + " lines/s");

    }

    private static void logWithStackTrace(Exception e, Path f) {
        if (debug) {
            debugErr("Error reading file " + f + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }


    private static void debugErr(String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }
}

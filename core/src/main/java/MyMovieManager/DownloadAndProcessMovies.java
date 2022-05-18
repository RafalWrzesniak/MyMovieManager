package MyMovieManager;

import Errors.MovieNotFoundException;
import FileOperations.IO;
import Internet.FilmwebClientActor;
import Internet.FilmwebClientMovie;
import Internet.WebOperations;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import Service.MovieCreatorService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public final class DownloadAndProcessMovies extends Thread {

//    == fields ==
    @Getter private List<Movie> downloadedMovies;
    @Getter private Map<File, Integer> movieFileMap;
    private static List<String> movieMessages;
    private static StringBuilder initDownLoadInfo = new StringBuilder();
    private static final TaskManager taskManager = TaskManager.getInstance();
    private static final FilmwebClientMovie filmwebClientMovie = FilmwebClientMovie.getInstance();
    private static final FilmwebClientActor filmwebClientActor = FilmwebClientActor.getInstance();

//    == required fields ==
    private final List<File> initFileList;
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allActors;
    private final List<String> actorStringList;

//    == methods ==
    @Override
    public void run() {
        setName("DAP");
        log.info("Thread \"{}\" started", getName());
        long startTime = System.nanoTime();
        movieMessages = new ArrayList<>();
        movieFileMap = new HashMap<>();
        downloadedMovies = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        List<String> readFileNames = new ArrayList<>();
        List<File> movieFileList = new ArrayList<>();
        initFileList.forEach(file -> {
            if(!readFileNames.contains(IO.removeFileExtension(file.getName()))) {
                readFileNames.add(IO.removeFileExtension(file.getName()));
                movieFileList.add(file);
            }
        });
        int numberOfThreads = movieFileList.size() > 20 ? 5 : 3;
        taskManager.addTask(movieFileList);
        for(int i = 0; i < numberOfThreads; i++) {
            Thread downloadAndProcess = new Thread(() -> {
                while (movieFileList.size() != 0) {
                    File movieFile;
                    synchronized (movieFileList) {
                        try {
                            movieFile = movieFileList.get(0);
                            movieFileList.remove(0);
                        } catch (IndexOutOfBoundsException ignored) {
                            log.debug("No more data to process for \"{}\"", Thread.currentThread().getName());
                            break;
                        }
                    }
                    Movie movie = handleMovieFromFile(movieFile, allMovies, allActors, actorStringList);
                    if(movie != null) {
                        downloadedMovies.add(movie);
                        movieFileMap.put(movieFile, movie.getId());
                    }
                }
            });
            downloadAndProcess.setName("DAP#" + i);
            threads.add(downloadAndProcess);
            downloadAndProcess.start();
        }

        for(Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.warn("Thread \"{}\" crashed", thread.getName());
            }
        }
        long estimatedTime = System.nanoTime() - startTime;
        initDownLoadInfo.append(String.format("For %s initial found string there was found %s movies.", initFileList.size(), downloadedMovies.size()));
        initDownLoadInfo.append(String.format("\nDownloading %s movies was accomplished in \"%s\" [s]", downloadedMovies.size(), ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100));
        writeFullDownLoadSummary();
        movieMessages = null;
        initDownLoadInfo = new StringBuilder();
        log.info("Thread \"" + getName() + "\" finished");
    }


    public static Movie handleMovieFromUrl(URL movieUrl, ContentList<Movie> allMovies, ContentList<Actor> allActors, List<String> actorStringList) {
        taskManager.addTask(movieUrl);
        MovieCreatorService movieCreatorService = new MovieCreatorService(filmwebClientMovie, filmwebClientActor, allMovies, allActors, actorStringList);
        StringBuilder finalMovieMessage = new StringBuilder();
        String downloadTimeMessage;
        Movie movie;
        long startTime = System.nanoTime();
        try {
            movie = movieCreatorService.createMovieFromUrl(movieUrl);
        } catch (MovieNotFoundException e) {
            finalMovieMessage.append(String.format("Unexpected error while downloading \"%s\" : \"%s\"", movieUrl, e.getMessage()));
            writeDownLoadSummary(finalMovieMessage.append(Arrays.toString(e.getStackTrace())).toString());
            taskManager.removeTask(movieUrl);
            return null;
        }
        long finalDownloadTime = System.nanoTime() - startTime;
        if(allMovies.add(movie)) {
            downloadTimeMessage = String.format("Movie \"%s\" downloaded and saved in \"%s\" [s]", movie, ((double) Math.round(finalDownloadTime/Math.pow(10, 7)))/100);
        } else {
            downloadTimeMessage = String.format("Movie \"%s\" already exists in the system, new data won't be downloaded", movie);
        }
        log.debug(downloadTimeMessage);
        finalMovieMessage.append(downloadTimeMessage).append("\n\n").append(movie.printPretty());
        writeDownLoadSummary(finalMovieMessage.toString());
        taskManager.removeTask(movieUrl);
        return movie;
    }

    public static Movie handleMovieFromFile(File movieFile, ContentList<Movie> allMovies, ContentList<Actor> allActors, List<String> actorStringList) {
        URL foundUrl;
        try {
            foundUrl = WebOperations.getMostSimilarTitleUrlFromQuery(IO.removeFileExtension(movieFile.getName()));
        } catch (IOException | MovieNotFoundException e) {
            e.printStackTrace();
            log.warn("Could not find any data from file {} because of {}", movieFile, e.getMessage());
            return null;
        }
        Movie movie = allMovies.getObjByUrlIfExists(foundUrl);
        if(movie == null) {
            return handleMovieFromUrl(foundUrl, allMovies, allActors, actorStringList);
        } else {
            log.info("Movie \"{}\" already exists on the list \"{}\", new data won't be downloaded", movie, allMovies);
            StringBuilder finalMovieMessage = new StringBuilder();
            finalMovieMessage.append(String.format("Movie \"%s\" already exists in the system, new data won't be downloaded", movie));
            if(movieMessages != null) {
                movieMessages.add(finalMovieMessage.toString());
            } else {
                writeDownLoadSummary(finalMovieMessage.toString());
            }
            return movie;
        }
    }

    private static void writeDownLoadSummary(String dataToWrite) {
        String fileName = "logs/DownloadSummary_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".xml";
        try (FileWriter myWriter = new FileWriter(fileName)) {
            myWriter.write(dataToWrite);
            log.debug("Summary download data properly saved in {}", fileName);
        } catch (IOException e) {
            log.warn("An error occurred while saving data to {}", fileName);
        }
    }

    private static void writeFullDownLoadSummary() {
        String fileName = "logs/DownloadSummary_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".xml";
        try (FileWriter myWriter = new FileWriter(fileName)) {
            myWriter.write(initDownLoadInfo.toString());
            for(String dataToWrite : movieMessages) {
                myWriter.write("\n\n===============================================================================================================\n\n");
                myWriter.write(dataToWrite);
            }
            log.debug("Summary download data properly saved in {}", fileName);
        } catch (IOException e) {
            log.warn("An error occurred while saving data to {}", fileName);
        }
    }
}

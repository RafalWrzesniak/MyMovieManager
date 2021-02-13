package MyMovieManager;

import Configuration.Files;
import FileOperations.IO;
import Internet.Connection;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public final class DownloadAndProcessMovies extends Thread {

//    == fields ==
    @Getter private List<Movie> downloadedMovies;
    @Getter private Map<File, Integer> movieFileMap;
    private static List<String> movieMessages;
    private static StringBuilder initDownLoadInfo = new StringBuilder();
    private static final TaskManager taskManager = TaskManager.getInstance();

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
        StringBuilder finalMovieMessage = new StringBuilder();
        String downloadTimeMessage;
        long startTime = System.nanoTime();
        Connection connection;
        Movie movie = null;
        try {
            connection = new Connection(movieUrl);
            movie = connection.createMovieFromFilmwebLink();
            if(allMovies.add(movie)) {
                connection.addCastToMovie(movie, allActors, actorStringList, allMovies);
                movie.printPretty();
                File movieDir = IO.createContentDirectory(movie);
                Path downloadedImagePath = Paths.get(movieDir.toString(), movie.getReprName().replaceAll(":", "").concat(".jpg"));
                if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                } else {
                    movie.setImagePath(Files.NO_MOVIE_COVER);
                }
                long estimatedTime = System.nanoTime() - startTime;
                downloadTimeMessage = String.format("Movie \"%s\" downloaded and saved in \"%s\" [s]", movie, ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100);
            } else {
                movie = allMovies.get(movie);
                downloadTimeMessage = String.format("Movie \"%s\" already exists in the system, new data won't be downloaded", movie);
            }
            log.debug(downloadTimeMessage);
            finalMovieMessage.append(downloadTimeMessage).append("\n\n").append(movie.printPretty()).append(connection.getFailedActorsMessage());
        } catch (IOException | NullPointerException e) {
            String failMessage = String.format("Unexpected error while downloading \"%s\" - \"%s\"", movieUrl, e.getMessage());
            finalMovieMessage.append(failMessage).append("\n");
            log.warn(failMessage);
        }
        writeDownLoadSummary(finalMovieMessage.toString());
        taskManager.removeTask(movieUrl);
        return movie;
    }

    public static Movie handleMovieFromFile(File movieFile, ContentList<Movie> allMovies, ContentList<Actor> allActors, List<String> actorStringList) {
        taskManager.addTask(movieFile);
        StringBuilder finalMovieMessage = new StringBuilder();
        String downloadTimeMessage;
        long startTime = System.nanoTime();
        Connection connection = null;
        Movie movie = null;
        try {
            connection = new Connection(IO.removeFileExtension(movieFile.getName()));
            movie = allMovies.getObjByUrlIfExists(connection.getMainMoviePage());
            if(movie != null) {
                log.info("Movie \"{}\" already exists on the list \"{}\", new data won't be downloaded", movie, allMovies);
                taskManager.removeTask(movieFile);
                finalMovieMessage.append(connection.getSearchingMessage());
                finalMovieMessage.append(String.format("Movie \"%s\" already exists in the system, new data won't be downloaded", movie));
                if(movieMessages != null) {
                    movieMessages.add(finalMovieMessage.toString());
                } else {
                    writeDownLoadSummary(finalMovieMessage.toString());
                }
                return movie;
            }
            movie = connection.createMovieFromFilmwebLink();
            if(allMovies.add(movie)) {
                connection.addCastToMovie(movie, allActors, actorStringList, allMovies);
                IO.createSummaryImage(movie, movieFile);
                File movieDir = IO.createContentDirectory(movie);
                Path downloadedImagePath = Paths.get(
                        movieDir.toString(),
                        movie.getReprName().replaceAll("[]?\\[*./:;|,\"]", "").concat(".jpg"));
                if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                } else {
                    movie.setImagePath(Files.NO_MOVIE_COVER);
                }
                long estimatedTime = System.nanoTime() - startTime;
                downloadTimeMessage = String.format("Movie \"%s\" downloaded and saved in \"%s\" [s]", movie, ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100);
            } else {
                movie = allMovies.get(movie);
                downloadTimeMessage = String.format("Movie \"%s\" already exists in the system, new data won't be downloaded", movie);
            }
            log.debug(downloadTimeMessage);
            finalMovieMessage.append(connection.getSearchingMessage()).append(downloadTimeMessage).append("\n\n").append(movie.printPretty()).append(connection.getFailedActorsMessage());
        } catch (IOException | NullPointerException e) {
            String failMessage = String.format("Unexpected error while downloading \"%s\" - \"%s\"", movieFile.getName(), e.getMessage());
            if(connection != null) {
                finalMovieMessage.append(connection.getSearchingMessage());
            }
            finalMovieMessage.append(failMessage).append("\n");
            log.warn(failMessage);
        }
        if(movieMessages != null) {
            movieMessages.add(finalMovieMessage.toString());
        } else {
            writeDownLoadSummary(finalMovieMessage.toString());
        }
        taskManager.removeTask(movieFile);
        return movie;
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

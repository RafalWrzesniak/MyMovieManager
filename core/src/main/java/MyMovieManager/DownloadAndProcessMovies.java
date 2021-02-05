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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final TaskManager taskManager = TaskManager.getInstance();

//    == required fields ==
    private final List<File> movieFileList;
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allActors;
    private final List<String> actorStringList;

//    == methods ==
    @Override
    public void run() {
        setName("DAP");
        log.info("Thread \"{}\" started", getName());
        movieFileMap = new HashMap<>();
        downloadedMovies = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
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
        log.info("Thread \"" + getName() + "\" finished");
    }


    public static void handleMovieFromUrl(URL movieUrl, ContentList<Movie> allMovies, ContentList<Actor> allActors, List<String> actorStringList) {
        taskManager.addTask(movieUrl);
        long startTime = System.nanoTime();
        Connection connection;
        Movie movie;
        try {
            connection = new Connection(movieUrl);
            movie = connection.createMovieFromFilmwebLink();
            if(allMovies.add(movie)) {
                connection.addCastToMovie(movie, allActors, actorStringList);
                movie.printPretty();
                File movieDir = IO.createContentDirectory(movie);
                Path downloadedImagePath = Paths.get(movieDir.toString(), movie.getReprName().replaceAll(":", "").concat(".jpg"));
                if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                } else {
                    movie.setImagePath(Files.NO_MOVIE_COVER);
                }
                long estimatedTime = System.nanoTime() - startTime;
                log.debug("Movie \"{}\" downloaded and saved in \"{}\" [s]", movie, ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100);
            } else {
                movie = allMovies.get(movie);
                log.debug("Movie \"{}\" already exists in system", movie);
            }

        } catch (IOException | NullPointerException e) {
            log.warn("Unexpected error while downloading from \"{}\" - \"{}\"", movieUrl, e.getMessage());
        }
        taskManager.removeTask(movieUrl);
    }

    public static Movie handleMovieFromFile(File movieFile, ContentList<Movie> allMovies, ContentList<Actor> allActors, List<String> actorStringList) {
        taskManager.addTask(movieFile);
        long startTime = System.nanoTime();
        Connection connection;
        Movie movie = null;
        try {
            connection = new Connection(IO.removeFileExtension(movieFile.getName()));
            movie = allMovies.getObjByUrlIfExists(connection.getMainMoviePage());
            if(movie != null) {
                log.info("Movie \"{}\" already exists on the list \"{}\", new data won't be downloaded", movie, allMovies);
                taskManager.removeTask(movieFile);
                return movie;
            }
            movie = connection.createMovieFromFilmwebLink();
            if(allMovies.add(movie)) {
                connection.addCastToMovie(movie, allActors, actorStringList);
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
                log.debug("Movie \"{}\" downloaded and saved in \"{}\" [s]", movie, ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100);
            } else {
                movie = allMovies.get(movie);
                log.debug("Movie \"{}\" already exists in system", movie);
            }
            movie.printPretty();
        } catch (IOException | NullPointerException e) {
            log.warn("Unexpected error while downloading \"{}\" - \"{}\"", movieFile.getName(), e);
        }
        taskManager.removeTask(movieFile);
        return movie;
    }

}

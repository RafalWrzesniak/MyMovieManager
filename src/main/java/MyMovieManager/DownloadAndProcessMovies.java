package MyMovieManager;

import Configuration.Config;
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

//    == required fields ==
    private final List<File> movieFileList;
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allActors;

//    == methods ==
    @Override
    public void run() {
        setName("DownloadAndProcess");
        log.info("Thread \"{}\" started", getName());
        movieFileMap = new HashMap<>();
        downloadedMovies = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int numberOfThreads = movieFileList.size() > 20 ? 5 : 3;
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
                    Movie movie = handleMovieFromFile(movieFile, allMovies, allActors);
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


    public static Movie handleMovieFromUrl(URL movieUrl, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        long startTime = System.nanoTime();
        Connection connection;
        Movie movie = null;
        try {
            connection = new Connection(movieUrl);
            movie = connection.createMovieFromFilmwebLink();
            if(allMovies.add(movie)) {
                connection.addCastToMovie(movie, allActors);
                movie.printPretty();
                File movieDir = IO.createContentDirectory(movie);
                Path downloadedImagePath = Paths.get(movieDir.toString(), movie.getReprName().replaceAll(":", "").concat(".jpg"));
                if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                } else {
                    movie.setImagePath(Config.NO_IMAGE);
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
        return movie;
    }

    public static Movie handleMovieFromFile(File movieFile, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        long startTime = System.nanoTime();
        Connection connection;
        Movie movie = null;
        try {
            connection = new Connection(movieFile.getName());
            movie = allMovies.getObjByUrlIfExists(connection.getMainMoviePage());
            if(movie != null) {
                log.info("Movie \"{}\" already exists on the list \"{}\", new data won't be downloaded", movie, allMovies);
                return movie;
            }
            movie = connection.createMovieFromFilmwebLink();
            if(allMovies.add(movie)) {
                connection.addCastToMovie(movie, allActors);
                IO.createSummaryImage(movie, movieFile);
                File movieDir = IO.createContentDirectory(movie);
                Path downloadedImagePath = Paths.get(
                        movieDir.toString(),
                        movie.getReprName().replaceAll("[]\\[*./:;|,\"]", "").concat(".jpg"));
                if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                } else {
                    movie.setImagePath(Config.NO_IMAGE);
                }
                long estimatedTime = System.nanoTime() - startTime;
                log.debug("Movie \"{}\" downloaded and saved in \"{}\" [s]", movie, ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100);
            } else {
                movie = allMovies.get(movie);
                log.debug("Movie \"{}\" already exists in system", movie);
            }
            movie.printPretty();
        } catch (IOException | NullPointerException e) {
            log.warn("Unexpected error while downloading \"{}\" - \"{}\"", movieFile.getName(), e.getMessage());
        }
        return movie;
    }

}

package MyMovieManager;

import FileOperations.IO;
import FileOperations.XMLOperator;
import Internet.Connection;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class DownloadAndProcessMovies extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(DownloadAndProcessMovies.class.getName());
    private final List<File> movieFileList;
    private final ContentList<Actor> allActors;
    private final ContentList<Movie> allMovies;

    private final List<Movie> downloadedMovies = new ArrayList<>();

    public DownloadAndProcessMovies(List<File> movieFileList, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        this.movieFileList = movieFileList;
        this.allMovies = allMovies;
        this.allActors = allActors;
        setName("DownloadAndProcess");
    }

    @Override
    public void run() {
        logger.info("Thread \"{}\" started", getName());
        List<Thread> threads = new ArrayList<>();
        int numberOfThreads = movieFileList.size() > 20 ? 5 : 3;
        for(int i = 0; i < numberOfThreads; i++) {
            Thread downloadAndProcess = new Thread(() -> {
                while (movieFileList.size() != 0) {
                    File movieFile;
                    synchronized (movieFileList) {
                        movieFile = movieFileList.get(0);
                        movieFileList.remove(0);
                    }
                    Movie movie = handleMovieFromFile(movieFile, allMovies, allActors);
                    if(movie != null) {
                        downloadedMovies.add(movie);
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
                logger.warn("Thread \"{}\" crashed", thread.getName());
            }
        }
        logger.info("Thread \"" + getName() + "\" finished");
    }


    public static Movie handleMovieFromUrl(URL movieUrl, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        Connection connection;
        Movie movie = null;
        try {
            connection = new Connection(movieUrl);
            movie = connection.createMovieFromFilmwebLink(allActors);
            allMovies.add(movie);
            movie.printPretty();
            File movieDir = IO.createContentDirectory(movie);
            Path downloadedImagePath = Paths.get(movieDir.toString(), movie.getReprName().replaceAll(":", "").concat(".jpg"));
            if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                movie.setImagePath(downloadedImagePath);
            } else {
                movie.setImagePath(IO.NO_IMAGE);
            }
        } catch (IOException | NullPointerException e) {
            logger.warn("Unexpected error while downloading from \"{}\" - \"{}\"", movieUrl, e.getMessage());
        }
        return movie;
    }

    public static Movie handleMovieFromFile(File movieFile, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        long startTime = System.nanoTime();
        Connection connection;
        Movie movie = null;
        try {
            connection = new Connection(movieFile.getName());
            movie = connection.createMovieFromFilmwebLink(allActors);
            allMovies.add(movie);
            movie.printPretty();
            IO.createSummaryImage(movie, movieFile);
            File movieDir = IO.createContentDirectory(movie);
            Path downloadedImagePath = Paths.get(
                    movieDir.toString(),
                    movie.getReprName().replaceAll("[]\\[*./:;|,\"]", "").concat(".jpg"));
            if( Connection.downloadImage(movie.getImageUrl(), downloadedImagePath) ) {
                movie.setImagePath(downloadedImagePath);
            } else {
                movie.setImagePath(IO.NO_IMAGE);
            }

            long estimatedTime = System.nanoTime() - startTime;
            logger.debug("Movie \"{}\" downloaded and saved in \"{}\" [s]", movie, ((double) Math.round(estimatedTime/Math.pow(10, 7)))/100);
        } catch (IOException | NullPointerException e) {
            logger.warn("Unexpected error while downloading \"{}\" - \"{}\"", movieFile.getName(), e.getMessage());
        }
        return movie;
    }

    public List<Movie> getDownloadedMovies() {
        return downloadedMovies;
    }
}

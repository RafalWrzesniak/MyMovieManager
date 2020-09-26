package MyMovieManager;

import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieMainFolder extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MovieMainFolder.class.getName());
    private static File MAIN_MOVIE_FOLDER;
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allActors;
    private final ContentList<Movie> moviesToWatch;


    public MovieMainFolder(ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        this.allMovies = allMovies;
        this.allActors = allActors;
        this.moviesToWatch = new ContentList<>(ContentList.MOVIES_TO_WATCH);
    }

    @Override
    public void run() {
        setName("MovieMainFolder");
        Map<File, Integer> lastRideMap = IO.readLastStateOfMainMovieFolder();
        List<File> currentState = IO.listDirectory(MAIN_MOVIE_FOLDER);
        List<File> newPositionsToHandle = new ArrayList<>();
        Map<File, Integer> newStateMap = new HashMap<>();
        for(File file : currentState) {
            if(!lastRideMap.containsKey(file)) {
                newPositionsToHandle.add(file);
            } else {
                newStateMap.put(file, lastRideMap.get(file));
            }
        }

        logger.debug("Found \"{}\" new movies in main movie folder", newPositionsToHandle.size());
        if(newPositionsToHandle.size() > 0) {
            DownloadAndProcessMovies downloadAndProcessMovies = new DownloadAndProcessMovies(newPositionsToHandle, allMovies, allActors);
            downloadAndProcessMovies.start();
            try {
                downloadAndProcessMovies.join();
            } catch (InterruptedException e) {
                logger.warn("Unexpected error while downloading new movies to main folder");
            }
            if(downloadAndProcessMovies.getDownloadedMovies().size() > 0) {
                newStateMap.putAll(downloadAndProcessMovies.getMovieFileMap());
            }
        }
        if(!newStateMap.equals(lastRideMap)) {
            IO.writeLastRideFile(newStateMap);
        }
        Path path = Paths.get(IO.getSavePathMovie().toString(), moviesToWatch.getListName().concat(".xml"));
        logger.debug("Attempt to remove \"{}\" ends with status \"{}\"", path.toString(), path.toFile().delete());
        for(Map.Entry<File, Integer> entry : newStateMap.entrySet()) {
            moviesToWatch.add(allMovies.getById(entry.getValue()));
        }
        logger.info("\"{}\" has now \"{}\" movies", ContentList.MOVIES_TO_WATCH, moviesToWatch.size());
    }

    public ContentList<Movie> getMoviesToWatch() {
        return moviesToWatch;
    }

    public static File getMainMovieFolder() {
        return MAIN_MOVIE_FOLDER;
    }

    public static void setMainMovieFolder(File mainMovieFolder) {
        MAIN_MOVIE_FOLDER = mainMovieFolder;
        IO.updateParamInCfg("MAIN_MOVIE_FOLDER", mainMovieFolder.toString());
    }







}

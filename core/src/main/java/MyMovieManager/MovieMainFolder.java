package MyMovieManager;

import Configuration.Config;
import FileOperations.IO;
import FileOperations.XMLOperator;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MovieMainFolder extends Thread {

//    == fields ==
    @Getter private final ContentList<Movie> moviesToWatch;
    @Getter private List<Movie> newMovies;

//    == required fields ==
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allActors;


//    == constructors ==
    @SneakyThrows
    public MovieMainFolder(ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        this.allMovies = allMovies;
        this.allActors = allActors;
        this.moviesToWatch = new ContentList<>(ContentList.MOVIES_TO_WATCH);
        Path path = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), moviesToWatch.getListName().concat(".xml"));
        Document doc = XMLOperator.createDocToRead(path.toFile());
        if(doc != null) {
            moviesToWatch.setDisplayName(doc.getElementsByTagName("displayName").item(0).getTextContent());
        }
        log.debug("Attempt to remove \"{}\" ends with status \"{}\"", path.toString(), path.toFile().delete());
    }

    public MovieMainFolder(ContentList<Movie> moviesToWatch, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
        this.allMovies = allMovies;
        this.allActors = allActors;
        this.moviesToWatch = moviesToWatch;
    }

//    == methods ==
    @Override
    public void run() {
        setName("MovieMain");
        if(allMovies == null || allActors == null || moviesToWatch == null) {
            log.warn("Passed arguments cannot be null, MainMovieFolder was not updated");
            return;
        }
        Map<File, Integer> lastRideMap = IO.readLastStateOfMainMovieFolder();
        List<File> currentState = IO.listDirectory(Configuration.Config.getMAIN_MOVIE_FOLDER().toFile());
        List<File> newPositionsToHandle = new ArrayList<>();
        Map<File, Integer> newStateMap = new HashMap<>();
        for(File file : currentState) {
            if(!lastRideMap.containsKey(file)) {
                newPositionsToHandle.add(file);
            } else {
                newStateMap.put(file, lastRideMap.get(file));
            }
        }

        log.debug("Found \"{}\" new movies in main movie folder", newPositionsToHandle.size());
        if(newPositionsToHandle.size() > 0) {
            DownloadAndProcessMovies downloadAndProcessMovies = new DownloadAndProcessMovies(newPositionsToHandle, allMovies, allActors);
            downloadAndProcessMovies.start();
            try {
                downloadAndProcessMovies.join();
            } catch (InterruptedException e) {
                log.warn("Unexpected error while downloading new movies to main folder");
            }
            if(downloadAndProcessMovies.getDownloadedMovies().size() > 0) {
                newStateMap.putAll(downloadAndProcessMovies.getMovieFileMap());
            }
            newMovies = downloadAndProcessMovies.getDownloadedMovies();
        }

        if(!newStateMap.equals(lastRideMap)) {
            IO.writeLastRideFile(newStateMap);
        }

        for(Map.Entry<File, Integer> entry : newStateMap.entrySet()) {
            moviesToWatch.add(allMovies.getById(entry.getValue()));
        }
        newStateMap.clear();
        log.info("\"{}\" has now \"{}\" movies", ContentList.MOVIES_TO_WATCH, moviesToWatch.size());
    }


}

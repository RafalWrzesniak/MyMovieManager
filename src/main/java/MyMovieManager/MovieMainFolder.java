package MyMovieManager;

import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MovieMainFolder extends Thread {

//    == constants ==
    @Getter private static File MAIN_MOVIE_FOLDER;

//    == fields ==
    @Getter private ContentList<Movie> moviesToWatch;

//    == required fields ==
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allActors;

//    == methods ==
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

        moviesToWatch = new ContentList<>(ContentList.MOVIES_TO_WATCH);
        Path path = Paths.get(IO.getSAVE_PATH_MOVIE().toString(), moviesToWatch.getListName().concat(".xml"));
        log.debug("Attempt to remove \"{}\" ends with status \"{}\"", path.toString(), path.toFile().delete());


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


    public static void setMainMovieFolder(File mainMovieFolder) {
        MAIN_MOVIE_FOLDER = mainMovieFolder;
        IO.updateParamInCfg("MAIN_MOVIE_FOLDER", mainMovieFolder.toString());
    }

}

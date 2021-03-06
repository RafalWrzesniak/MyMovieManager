package MyMovieManager;

import Configuration.Config;
import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;

class MovieMainFolderTest {

    private static Path currentMovieMainFolder;
    private static final Path testMovieMain = Configuration.Files.TMP_FILES.resolve("testMovieMain");

    private final Movie joker = new Movie(Map.ofEntries(
            Map.entry(Movie.TITLE, Collections.singletonList("Joker")),
            Map.entry(Movie.PREMIERE, Collections.singletonList("2019-08-31")),
            Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Joker-2019-810167"))
    ), true);
    private final Movie kiler = new Movie(Map.ofEntries(
            Map.entry(Movie.TITLE, Collections.singletonList("Kiler")),
            Map.entry(Movie.PREMIERE, Collections.singletonList("1997-11-17")),
            Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Kiler-1997-529"))
            ), true);

    private final Movie deadpool = new Movie(Map.ofEntries(
            Map.entry(Movie.TITLE, Collections.singletonList("Deadpool")),
            Map.entry(Movie.PREMIERE, Collections.singletonList("2016-01-21")),
            Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Deadpool-2016-514675"))
    ), true);

    static void clearTmp() {
        IO.deleteDirectoryRecursively(Configuration.Files.TMP_FILES.toFile());
        System.out.println(Configuration.Files.TMP_FILES.toFile().mkdirs());
        Config.setSAVE_PATH(Configuration.Files.TMP_FILES.toFile(), false);
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        clearTmp();
        Config.setSAVE_PATH(Configuration.Files.TMP_FILES.toFile(), false);
        currentMovieMainFolder = Config.getMAIN_MOVIE_FOLDER();
        if(testMovieMain.toFile().mkdir()) {
            File movie1 = testMovieMain.resolve("Joker").toFile();
            movie1.mkdir();
            File movie2 = testMovieMain.resolve("Kiler").toFile();
            movie2.mkdir();
        }
        Config.setMAIN_MOVIE_FOLDER(testMovieMain);

        Files.move(Configuration.Files.LAST_RIDE.toPath(), Configuration.Files.TMP_FILES.resolve(Configuration.Files.LAST_RIDE.getName()), REPLACE_EXISTING);

        Path newLastRide = Paths.get("src","test", "resources", "lastRideTest.xml");
        Files.copy(newLastRide, Configuration.Files.LAST_RIDE.toPath(), REPLACE_EXISTING);
    }

    @AfterAll
    static void afterAll() throws IOException {
        Config.setSAVE_PATH(new File(System.getProperty("user.dir").concat("\\savedData")), false);
        Config.setMAIN_MOVIE_FOLDER(currentMovieMainFolder);
        Files.move(Configuration.Files.TMP_FILES.resolve(Configuration.Files.LAST_RIDE.getName()), Configuration.Files.LAST_RIDE.toPath(), REPLACE_EXISTING);
    }


    @Test
    void movieMainFolderTest() throws InterruptedException, Config.ArgumentIssue {
        ContentList<Movie> allMovies = new ContentList<>("allTestMovies");
        allMovies.add(joker);
        ContentList<Actor> allActors = new ContentList<>("allTestActors");
        MovieMainFolder movieMainFolder = new MovieMainFolder(allMovies, allActors);
        movieMainFolder.start();
        movieMainFolder.join();
        assertEquals(2, movieMainFolder.getMoviesToWatch().size());
        assertTrue(movieMainFolder.getMoviesToWatch().contains(joker));
        assertTrue(movieMainFolder.getMoviesToWatch().contains(kiler));

        File movie3 = testMovieMain.resolve("Deadpool").toFile();
        movie3.mkdir();

        MovieMainFolder movieMainFolder2 = new MovieMainFolder(movieMainFolder.getMoviesToWatch(), allMovies, allActors, null);
        movieMainFolder2.start();
        movieMainFolder2.join();
        assertEquals(3, movieMainFolder.getMoviesToWatch().size());
        assertTrue(movieMainFolder.getMoviesToWatch().contains(joker));
        assertTrue(movieMainFolder.getMoviesToWatch().contains(kiler));
        assertTrue(movieMainFolder.getMoviesToWatch().contains(deadpool));
    }
}
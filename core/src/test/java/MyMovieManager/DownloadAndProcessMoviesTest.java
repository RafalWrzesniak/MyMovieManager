package MyMovieManager;

import Configuration.Config;
import Configuration.Files;
import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import org.junit.jupiter.api.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;


class DownloadAndProcessMoviesTest {

       private final static ContentList<Movie> allMovies = new ContentList<>("allMoviesTest");
       private final static ContentList<Actor> allActors = new ContentList<>("allActorsTest");

    @BeforeEach
    void clearTmp() {
        IO.deleteDirectoryRecursively(Files.TMP_FILES.toFile());
        System.out.println(Files.TMP_FILES.toFile().mkdirs());
        Config.setSAVE_PATH(Files.TMP_FILES.toFile(), false);
    }

    @BeforeAll
    static void beforeAll() {
        Config.setSAVE_PATH(Files.TMP_FILES.toFile(), false);
    }

    @AfterAll
    static void afterAll() {
        Config.setSAVE_PATH(new File(System.getProperty("user.dir").concat("\\savedData")), false);
    }

    @Test
    void handleListOfMoviesWithThreads() throws MalformedURLException, InterruptedException {
        URL url1 = new URL("https://www.filmweb.pl/film/Geneza+planety+ma%C5%82p-2011-558709");
        URL url2 = new URL("https://www.filmweb.pl/film/Ewolucja+planety+ma%C5%82p-2014-644064");
        URL url3 = new URL("https://www.filmweb.pl/film/Wojna+o+planet%C4%99+ma%C5%82p-2017-700190");
        File movieFile1 = Files.TMP_FILES.resolve("movies").resolve("Geneza planety małp").toFile();
        File movieFile2 = Files.TMP_FILES.resolve("movies").resolve("Ewolucja planety małp").toFile();
        File movieFile3 = Files.TMP_FILES.resolve("movies").resolve("Wojna o planetę małp").toFile();
        if(movieFile1.mkdirs() && movieFile2.mkdirs() && movieFile3.mkdirs()) System.out.println("All ready");
        DownloadAndProcessMovies dap = new DownloadAndProcessMovies(IO.listDirectory(Files.TMP_FILES.resolve("movies").toFile()), allMovies, allActors, null);
        dap.start();
        dap.join();

        assertEquals("Geneza planety małp", allMovies.getObjByUrlIfExists(url1).getTitle());
        assertEquals("Ewolucja planety małp", allMovies.getObjByUrlIfExists(url2).getTitle());
        assertEquals("Wojna o planetę małp", allMovies.getObjByUrlIfExists(url3).getTitle());

        assertEquals(3, dap.getDownloadedMovies().size());
        assertEquals(3, dap.getMovieFileMap().size());

    }

    @Test
    void handleMovieFromUrl() throws MalformedURLException {
        int numberOfFiles = IO.listDirectory(Config.getSAVE_PATH_MOVIE().toFile()).size();
        URL url = new URL("https://www.filmweb.pl/film/12+ma%C5%82p-1995-1210");
        DownloadAndProcessMovies.handleMovieFromUrl(url, allMovies, allActors, null);

        assertEquals(IO.listDirectory(Config.getSAVE_PATH_MOVIE().toFile()).size(), numberOfFiles + 1);
        assertEquals("12 małp", allMovies.getObjByUrlIfExists(url).getTitle());
    }

    @Test
    void handleMovieFromFile() throws MalformedURLException {
        URL url = new URL("https://www.filmweb.pl/film/Planeta+ma%C5%82p-2001-8692");
        File movieFile = Config.getSAVE_PATH_MOVIE().resolve("Planeta małp").toFile();
        System.out.println(movieFile.mkdir());
        int numberOfFiles = IO.listDirectory(movieFile).size();

        DownloadAndProcessMovies.handleMovieFromFile(movieFile, allMovies, allActors, null);

        assertEquals(IO.listDirectory(movieFile).size(), numberOfFiles + 1);
        assertEquals("Planeta małp", allMovies.getObjByUrlIfExists(url).getTitle());

    }

}
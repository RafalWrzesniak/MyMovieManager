package Internet;

import Configuration.Config;
import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {

    URL movieUrl = new URL("https://www.filmweb.pl/film/Kiler-1997-529");
    URL actorURL = new URL("https://www.filmweb.pl/person/Jennifer+Aniston-6757");

    ConnectionTest() throws MalformedURLException {
    }

    @BeforeAll
    static void beforeAll() {
        Config.setSAVE_PATH(Configuration.Files.TMP_FILES.toFile(), false);
    }

    @AfterAll
    static void afterAll() {
        Config.setSAVE_PATH(Configuration.Files.DEFAULT_SAVED_DATA.toFile(), false);
    }

    @BeforeEach
    void clearTmp() {
        IO.deleteDirectoryRecursively(Configuration.Files.TMP_FILES.toFile());
        if(Configuration.Files.TMP_FILES.toFile().mkdirs()) {
            Config.setSAVE_PATH(Configuration.Files.TMP_FILES.toFile(), false);
        }
    }

    @Test
    void downloadWebsiteTest() throws IOException {
        Connection connection = new Connection(movieUrl);
        File file = connection.downloadWebsite();
        assertTrue(Files.size(file.toPath()) > 100000);
        assertTrue(IO.listDirectory(Configuration.Files.TMP_FILES.toFile()).contains(file));
    }

    @Test
    void downloadImageTest() throws IOException {
        Connection connection = new Connection(actorURL);
        URL imageUrl = connection.getImageUrl(false);
        Path file = Configuration.Files.TMP_FILES.resolve("image.jpg");
        Connection.downloadImage(imageUrl, file);
        assertTrue(Files.size(file) > 15000);
        assertTrue(IO.listDirectory(Configuration.Files.TMP_FILES.toFile()).contains(file.toFile()));
    }

    @Test
    void createActorFromLinkTest() throws IOException {
        Connection connection = new Connection(actorURL);
        Actor actor = connection.createActorFromFilmwebLink();

        assert actor != null;
        assertEquals("Jennifer Aniston", actor.getNameAndSurname());
        assertEquals(LocalDate.of(1969, 2, 11), actor.getBirthday());
        assertEquals("USA", actor.getNationality());
        assertNull(actor.getDeathDay());
        assertEquals(actorURL, actor.getFilmweb());
    }

    @Test
    void returnEmptyListWhenWrongActorUrlsProvided() throws IOException, Config.ArgumentIssue {
        ContentList<Actor> allActors = new ContentList<>("test0");
        Connection connection = new Connection(movieUrl);
        List<String> emptyList = new ArrayList<>();

        assertEquals(new ArrayList<>(), connection.createActorsFromFilmwebLinks(emptyList, allActors));
        assertEquals(new ArrayList<>(), connection.createActorsFromFilmwebLinks(null, allActors));
        assertEquals(new ArrayList<>(), connection.createActorsFromFilmwebLinks(emptyList, null));
    }

    @Test
    void createFewActorsFromMethod() throws IOException, Config.ArgumentIssue {
        ContentList<Actor> allActors = new ContentList<>("test1");
        List<String> actorUrls = List.of(actorURL.toString(), "https://www.filmweb.pl/person/Tom+Hanks-124", actorURL.toString());
        Connection connection = new Connection(movieUrl);
        List<Actor> createdActors = connection.createActorsFromFilmwebLinks(actorUrls, allActors);
        assertEquals(3, createdActors.size());
        assertEquals(2, allActors.size());
        assertEquals("Jennifer Aniston", createdActors.get(0).getNameAndSurname());
        assertEquals("Tom Hanks", createdActors.get(1).getNameAndSurname());
    }

    @Test
    void createMovieFromUrl() throws IOException {
        Connection connection = new Connection(movieUrl);
        Movie movie = connection.createMovieFromFilmwebLink();
        assertEquals("Kiler", movie.getTitle());
        assertEquals(LocalDate.of(1997, 11, 17), movie.getPremiere());
        assertEquals("Polska", movie.getProduction().get(0));
    }

    @Test
    void addCastToMovie() throws IOException, Config.ArgumentIssue {
        ContentList<Actor> allActors = new ContentList<>("test2");
        Connection connection = new Connection(movieUrl);
        Movie movie = connection.createMovieFromFilmwebLink();
        connection.addCastToMovie(movie, allActors);

        assertEquals(1, movie.getWriters().size());
        assertEquals(1, movie.getDirectors().size());
        assertEquals(10, movie.getCast().size());
    }

    @Test
    void findProperLinkInFilmwebSearch() throws IOException {
        Connection connection;

        connection = new Connection("Wilk z Wall Street");
        assertEquals(new URL("https://www.filmweb.pl/film/Wilk+z+Wall+Street-2013-426597"), connection.getMainMoviePage());

        connection = new Connection("Kiler");
        assertEquals(new URL("https://www.filmweb.pl/film/Kiler-1997-529"), connection.getMainMoviePage());

        connection = new Connection("Morderstwo w Orient Expressie (1974)");
        assertEquals(new URL("https://www.filmweb.pl/film/Morderstwo+w+Orient+Expressie-1974-7846"), connection.getMainMoviePage());
    }
}
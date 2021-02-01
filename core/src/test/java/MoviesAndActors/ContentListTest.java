package MoviesAndActors;

import Configuration.Config;
import Configuration.Files;
import FileOperations.IO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContentListTest {

    static ContentList<Actor> contentListActor;
    static ContentList<Movie> contentListMovie;

    Movie movie = new Movie(Map.ofEntries(
            Map.entry(Movie.TITLE, Collections.singletonList("Most szpiegów")),
            Map.entry(Movie.PREMIERE, Collections.singletonList("2015-10-16")),
            Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Most+szpieg%C3%B3w-2015-728144"))
    ), true);
    Movie movie2 = new Movie(Map.ofEntries(
            Map.entry(Movie.TITLE, Collections.singletonList("Birdman")),
            Map.entry(Movie.PREMIERE, Collections.singletonList("2001-12-13")),
            Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Birdman-2014-680709"))
    ), true);
    private final Actor actor = new Actor(Map.ofEntries(
            Map.entry(Actor.NAME, "Cezary"),
            Map.entry(Actor.SURNAME, "Pazura"),
            Map.entry(Actor.NATIONALITY, "Poland"),
            Map.entry(Actor.BIRTHDAY, "1962-06-13"),
            Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone9"),
            Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")
    ));
    private final Actor actor2 = new Actor(Map.ofEntries(
            Map.entry(Actor.NAME, "Cezary2"),
            Map.entry(Actor.SURNAME, "Pazura2"),
            Map.entry(Actor.NATIONALITY, "Poland"),
            Map.entry(Actor.BIRTHDAY, "1962-06-13"),
            Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone99"),
            Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")
    ));

    @BeforeEach
    void clearTmp() {
        IO.deleteDirectoryRecursively(Files.TMP_FILES.toFile());
        System.out.println(Files.TMP_FILES.toFile().mkdirs());
        Config.setSAVE_PATH(Files.TMP_FILES.toFile(), false);
    }

    @BeforeAll
    static void beforeAll() throws Config.ArgumentIssue {
        contentListActor = new ContentList<>("myActorListTest");
        contentListMovie = new ContentList<>("myMovieListTest");
        Config.setSAVE_PATH(Files.TMP_FILES.toFile(), false);
    }

    @AfterAll
    static void afterAll() {
        Config.setSAVE_PATH(new File(System.getProperty("user.dir").concat("\\savedData")), false);
    }


    @Test
    void getListName() throws Config.ArgumentIssue {
        assertEquals("myActorListTest", contentListActor.getListName());
        assertEquals("someOtherList", new ContentList<Actor>("someOtherList").getListName());
    }


    @Test
    void addAndContains() {
        contentListActor.add(actor);
        assertTrue(contentListActor.contains(actor));
        contentListActor.remove(actor);

        contentListMovie.add(movie2);
        assertTrue(contentListMovie.contains(movie2));
        contentListMovie.remove(movie2);
    }

    @Test
    void addAll() {
        contentListMovie.clear();
        List<Movie> movieList2 = List.of(movie2, movie);
        contentListMovie.addAll(movieList2);
        assertTrue(contentListMovie.contains(movie) && contentListMovie.contains(movie2));
    }


    @Test
    void getList() {
        contentListActor.add(actor);
        List<Actor> tmpList = new ArrayList<>();
        tmpList.add(actor);
        assertEquals(tmpList, contentListActor.getList());
    }


    @Test
    void indexOf() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals(0, contentListActor.indexOf(actor));
        assertEquals(1, contentListActor.indexOf(actor2));
    }

    @Test
    void get() {
        contentListActor.add(actor);
        assertEquals(actor, contentListActor.get(0));
    }

    @Test
    void size() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals(2, contentListActor.size());
    }

    @Test
    void remove() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals(2, contentListActor.size());
        contentListActor.remove(actor);
        contentListActor.remove(0);
        assertEquals(0, contentListActor.size());
    }


    @Test
    void clear() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals(2, contentListActor.size());
        contentListActor.clear();
        assertEquals(0, contentListActor.size());
    }

    @Test
    void find() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals(2, contentListActor.find("Cezary").size());
        assertEquals(0, contentListActor.find("NoJack").size());
    }

//    @Test
//    void getById() {
//        contentListActor.add(actor);
//        contentListActor.add(actor2);
//        assertEquals(actor, contentListActor.getById(0));
//        assertEquals(actor2, contentListActor.getById(1));
//    }

//    @Test
//    void convertStrIdsToObjects() {
//        contentListMovie.add(movie);
//        contentListMovie.add(movie2);
//        List<Movie> tmpList = contentListMovie.convertStrIdsToObjects(List.of("2", "3"));
//        assertEquals(contentListMovie.getList(), tmpList);
//    }

    @Test
    void isEmpty() {
        contentListMovie.clear();
        assertTrue(contentListMovie.isEmpty());
    }

    @Test
    void getContentListFromListByName() throws Config.ArgumentIssue {
        List<ContentList<Movie>> listOfContentLists = List.of(contentListMovie, new ContentList<>("otherList"));
        assertEquals(contentListMovie, ContentList.getContentListFromListByName(listOfContentLists, "myMovieListTest"));
    }

    @Test
    void testToString() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals("ContentList{name='\u001B[1mmyActorListTest\u001B[0m', size='\u001B[1m2\u001B[0m'}", contentListActor.toString());
    }
}
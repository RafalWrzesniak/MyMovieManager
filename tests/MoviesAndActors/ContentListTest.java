package MoviesAndActors;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentListTest {

    private final ContentList<Actor> contentListActor = new ContentList<>("myActorList");
    private final ContentList<Movie> contentListMovie = new ContentList<>("myMovieList");
    private final Movie movie = new Movie("Most szpiegów", LocalDate.of(2015, 10, 16));
    private final Movie movie2 = new Movie("Most szpiegów 2", LocalDate.of(2015, 10, 16));
    private final Actor actor = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "www.filmweb.pl", "E:\\xInne\\dk.jpg");
    private final Actor actor2 = new Actor("Jack2", "Sparrow2", "Karaibian", "1957-06-02", "www.filmweb.pl", "E:\\xInne\\dk.jpg");

    @Test
    void getListName() {
        assertEquals("myActorList", contentListActor.getListName());
        assertEquals("someOtherList", new ContentList<Actor>("someOtherList").getListName());
    }


    @Test
    void addAndContains() {
        contentListActor.add(actor);
        assertTrue(contentListActor.contains(actor));
        contentListActor.remove(actor);

        contentListMovie.add(movie);
        assertTrue(contentListMovie.contains(movie));
        contentListMovie.remove(movie);
    }

    @Test
    void addAll() {
        List<Movie> movieList = List.of(movie, movie2);
        contentListMovie.addAll(movieList);
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
        assertEquals(2, contentListActor.find("Jack").size());
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
        assertTrue(contentListMovie.isEmpty());
    }

    @Test
    void getContentListFromListByName() {
        List<ContentList<Movie>> listOfContentLists = List.of(contentListMovie, new ContentList<>("otherList"));
        assertEquals(contentListMovie, ContentList.getContentListFromListByName(listOfContentLists, "myMovieList"));
    }

    @Test
    void testToString() {
        contentListActor.add(actor);
        contentListActor.add(actor2);
        assertEquals(List.of(actor, actor2).toString(), contentListActor.toString());
    }
}
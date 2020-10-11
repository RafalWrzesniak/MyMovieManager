package MoviesAndActors;

import Configuration.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    static Collection<Object[]> stringParams() {
        return Arrays.asList(new Object[][]{
                {Map.ofEntries(
                        Map.entry(Actor.NAME, "Rafał"),
                        Map.entry(Actor.SURNAME, "Wrześniak"),
                        Map.entry(Actor.NATIONALITY, "Poland"),
                        Map.entry(Actor.BIRTHDAY, "1994-08-11"),
                        Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone0"),
                        Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\me.jpg")
                ), 26},
                {Map.ofEntries(
                        Map.entry(Actor.NAME, "Steven"),
                        Map.entry(Actor.SURNAME, "Spielberg"),
                        Map.entry(Actor.NATIONALITY, "USA"),
                        Map.entry(Actor.BIRTHDAY, "1946-12-18"),
                        Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone1"),
                        Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\ss.jpg")
                ), 74},
                {Map.ofEntries(
                        Map.entry(Actor.NAME, "Cezary"),
                        Map.entry(Actor.SURNAME, "Pazura"),
                        Map.entry(Actor.NATIONALITY, "Poland"),
                        Map.entry(Actor.BIRTHDAY, "1962-06-13"),
                        Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone2"),
                        Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")
                ), 58},
                {Map.ofEntries(
                        Map.entry(Actor.NAME, "Jeniffer"),
                        Map.entry(Actor.SURNAME, "Aniston"),
                        Map.entry(Actor.NATIONALITY, "USA"),
                        Map.entry(Actor.BIRTHDAY, "1969-02-11"),
                        Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone3"),
                        Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\ja.jpg")
                ), 51},
                {Map.ofEntries(
                        Map.entry(Actor.NAME, "Tom"),
                        Map.entry(Actor.SURNAME, "Hanks"),
                        Map.entry(Actor.NATIONALITY, "USA"),
                        Map.entry(Actor.BIRTHDAY, "1956-07-07"),
                        Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone4"),
                        Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\th.jpg")
                ), 64},
        });
    }

    Actor actor;
    Movie most = new Movie(Map.ofEntries(
            Map.entry(Movie.TITLE, Collections.singletonList("Most szpiegów")),
            Map.entry(Movie.PREMIERE, Collections.singletonList("2015-10-16")),
            Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Most+szpieg%C3%B3w-2015-728144"))
            ));
    Movie birdman = new Movie(Map.ofEntries(
        Map.entry(Movie.TITLE, Collections.singletonList("Birdman")),
        Map.entry(Movie.PREMIERE, Collections.singletonList("2001-12-13")),
        Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Birdman-2014-680709"))
        ));

    Movie lotr = new Movie(Map.ofEntries(
        Map.entry(Movie.TITLE, Collections.singletonList("Władca pierścieni: Drużyna Pierścienia")),
        Map.entry(Movie.PREMIERE, Collections.singletonList("2001-12-13")),
        Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/W%C5%82adca+Pier%C5%9Bcieni%3A+Dru%C5%BCyna+Pier%C5%9Bcienia-2001-1065"))
        ));
    Movie deadpool = new Movie(Map.ofEntries(
        Map.entry(Movie.TITLE, Collections.singletonList("Deadpool")),
        Map.entry(Movie.PREMIERE, Collections.singletonList("2016-01-21")),
        Map.entry(Movie.FILMWEB, Collections.singletonList("https://www.filmweb.pl/film/Deadpool-2016-514675"))
        ));
        
    void setUp() {
        actor = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Jack"),
                Map.entry(Actor.SURNAME, "Sparrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone999"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));
    }




    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getName(Map<String, String> map) {
        Actor person = new Actor(map);
        assertEquals(map.get(Actor.NAME), person.getName());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getSurname(Map<String, String> map) {
        Actor person = new Actor(map);
        assertEquals(map.get(Actor.SURNAME), person.getSurname());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getNationality(Map<String, String> map) {
        Actor person = new Actor(map);
        assertEquals(map.get(Actor.NATIONALITY), person.getNationality());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getBirthday(Map<String, String> map) {
        Actor person = new Actor(map);
        assertEquals(map.get(Actor.BIRTHDAY), person.getBirthday().toString());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getAge(Map<String, String> map, int age) {
        Actor person = new Actor(map);
        assertEquals(age, person.getAge());
    }


    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getImagePath(Map<String, String> map) {
        Actor person = new Actor(map);
        assertEquals(map.get(Actor.IMAGE_PATH), person.getImagePath().toString());
    }

    @org.junit.jupiter.api.Test
    void checkForNullOrEmptyOrIllegalChar() {
        Config.ArgumentIssue exception = assertThrows(Config.ArgumentIssue.class,
            () -> ContentType.checkForNullOrEmptyOrIllegalChar(null, "Name"));
        assertEquals("Name argument cannot be null!", exception.getMessage());

        Config.ArgumentIssue exception2 = assertThrows(Config.ArgumentIssue.class,
                () -> ContentType.checkForNullOrEmptyOrIllegalChar("", "Surname"));
        assertEquals("Surname argument cannot be empty!", exception2.getMessage());

    }

    @org.junit.jupiter.api.Test
    void testEquals() {
        Actor person = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Jack"),
                Map.entry(Actor.SURNAME, "Sparrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone1"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));
        Actor person2 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Khan"),
                Map.entry(Actor.SURNAME, "Arrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone2"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));
        assertFalse(person.equals(person2));

    }

//    @org.junit.jupiter.api.Test
//    void testToString() {
//        Actor person = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "www.filmweb.pl", "E:\\xInne\\dk.jpg");
//
//        String expected = "Actor{name='Jack', surname='Sparrow', age=63}";
//        assertEquals(expected, person.toString());
//    }

    @Test
    void getAllMoviesActorPlayedIn() {
        setUp();
        actor.addMovieActorPlayedIn(most);
        actor.addMovieActorPlayedIn(birdman);
        actor.addMovieActorPlayedIn(lotr);
        List<Movie> tab = Arrays.asList(most, birdman, lotr);
        assertArrayEquals(tab.toArray(), actor.getAllMoviesActorPlayedIn().toArray());
    }


    @Test
    void isPlayingIn() {
        setUp();
        actor.addMovieActorPlayedIn(lotr);
        assertTrue(actor.isPlayingIn(lotr));
        assertFalse(actor.isPlayingIn(deadpool));
    }


    @Test
    void addMovieActorPlayedIn() {
        setUp();
        actor.addMovieActorPlayedIn(deadpool);
        assertTrue(actor.isPlayingIn(deadpool));
    }

    @Test
    void addSeveralMoviesToActor() {
        setUp();
        actor.addMovieActorPlayedIn(most);
        actor.addMovieActorPlayedIn(birdman);
        actor.addMovieActorPlayedIn(lotr);
        List<Movie> tab = Arrays.asList(most, birdman, lotr);
        actor.addSeveralMoviesToActor(tab);
        assertTrue(actor.isPlayingIn(most));
        assertTrue(actor.isPlayingIn(birdman));
        assertTrue(actor.isPlayingIn(birdman));
        assertFalse(actor.isPlayingIn(deadpool));
    }


    @Test
    void getAllMoviesDirectedBy() {
        setUp();
        actor.addMovieDirectedBy(most);
        actor.addMovieDirectedBy(birdman);
        actor.addMovieDirectedBy(lotr);
        List<Movie> tab = Arrays.asList(most, birdman, lotr);
        assertArrayEquals(tab.toArray(), actor.getAllMoviesDirectedBy().toArray());
    }

    @Test
    void isDirecting() {
        setUp();
        actor.addMovieDirectedBy(most);
        assertTrue(actor.isDirecting(most));
        assertFalse(actor.isDirecting(birdman));
    }

    @Test
    void addMovieDirectedBy() {
        setUp();
        actor.addMovieDirectedBy(deadpool);
        assertTrue(actor.isDirecting(deadpool));
    }


    @Test
    void getAllMoviesWrittenBy() {
        setUp();
        actor.addMovieWrittenBy(most);
        actor.addMovieWrittenBy(birdman);
        actor.addMovieWrittenBy(lotr);
        List<Movie> tab = Arrays.asList(most, birdman, lotr);
        assertArrayEquals(tab.toArray(), actor.getAllMoviesWrittenBy().toArray());
    }

    @Test
    void isWriting() {
        setUp();
        actor.addMovieWrittenBy(most);
        assertTrue(actor.isWriting(most));
        assertFalse(actor.isWriting(birdman));
    }

    @Test
    void addMovieWrittenBy() {
        setUp();
        actor.addMovieWrittenBy(deadpool);
        assertTrue(actor.isWriting(deadpool));
    }


    @Test
    void equals() {
        setUp();
        Actor actor2 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Jack"),
                Map.entry(Actor.SURNAME, "Sparrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone999"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));
        Actor actor3 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Khan"),
                Map.entry(Actor.SURNAME, "Arrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone9"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));
        assertEquals(actor2, actor);
        assertNotEquals(actor3, actor);
    }

    @Test
    void compareTo() {
        setUp();        
        Actor actor2 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Jack"),
                Map.entry(Actor.SURNAME, "Sparrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone999"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));        
        Actor actor3 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Khan"),
                Map.entry(Actor.SURNAME, "Arrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "https://www.filmweb.pl/person/someone123"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\dk.jpg")
        ));
        assertEquals(0, actor.compareTo(actor2));
        assertEquals(-1, actor.compareTo(actor3));
    }

    @Test
    void searchFor() {
        setUp();
        assertTrue(actor.searchFor("arro"));
        assertTrue(actor.searchFor("Main Kara"));
        assertFalse(actor.searchFor("nope"));
    }





}
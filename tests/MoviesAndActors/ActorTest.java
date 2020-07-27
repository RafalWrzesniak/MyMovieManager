package MoviesAndActors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    static Collection<Object[]> stringParams() {
        return Arrays.asList(new Object[][]{
                {"Rafał", "Wrześniak", "Poland", LocalDate.of(1994, 8, 11), "E:\\xInne\\me.jpg", 26, 0},
                {"Steven", "Spielberg", "USA", LocalDate.of(1946, 12, 18), "E:\\xInne\\ss.jpg", 74, 1},
                {"Cezary", "Pazura", "Poland", LocalDate.of(1962, 6, 13), "E:\\xInne\\cp.jpg", 58, 2},
                {"Jeniffer", "Aniston", "USA", LocalDate.of(1969, 2, 11), "E:\\xInne\\ja.jpg", 51, 3},
                {"Tom", "Hanks", "USA", LocalDate.of(1956, 7, 9), "E:\\xInne\\th.jpg", 64, 4}
        });
    }

    Actor actor;
    Movie most = new Movie("Most szpiegów", LocalDate.of(2015, 10, 16));
    Movie birdman = new Movie("Birdman", LocalDate.of(2014, 8, 27));
    Movie lotr = new Movie("Władca pierścieni: Drużyna Pierścienia", LocalDate.of(2001, 12, 13));
    Movie deadpool = new Movie("Deadpool", LocalDate.of(2016, 1, 21));    
        
    void setUp() {
        actor = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "E:\\xInne\\dk.jpg");
    }




    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getName(String name, String surname, String nationality, String birthday, String imagePath) {
        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(name, person.getName());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getSurname(String name, String surname, String nationality, String birthday, String imagePath) {
        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(surname, person.getSurname());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getNationality(String name, String surname, String nationality, String birthday, String imagePath) {
        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(nationality, person.getNationality());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getBirthday(String name, String surname, String nationality, String birthday, String imagePath) {
        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(birthday, person.getBirthday());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getAge(String name, String surname, String nationality, String birthday, String imagePath, int age) {
        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(age, person.getAge());
    }

//    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
//    @MethodSource("stringParams")
//    void getPersonId(String name, String surname, String nationality, LocalDate birthday, String imagePath, int age, int id) {
//        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
//        assertEquals(id, person.getPersonId());
//    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getImagePath(String name, String surname, String nationality, String birthday, String imagePath) {
        Actor person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(imagePath, person.getImagePath());
    }

    @org.junit.jupiter.api.Test
    void checkForNullOrEmptyOrIllegalChar() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Actor.checkForNullOrEmptyOrIllegalChar(null, "Name"));
        assertEquals("Name argument cannot be null!", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> Actor.checkForNullOrEmptyOrIllegalChar("", "Surname"));
        assertEquals("Surname argument cannot be empty!", exception2.getMessage());

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                () -> Actor.checkForNullOrEmptyOrIllegalChar("as[d", "Nationality"));
        assertEquals("Nationality argument contains illegal char: '['", exception3.getMessage());
    }

    @org.junit.jupiter.api.Test
    void testEquals() {
        Actor person = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "E:\\xInne\\dk.jpg");
        Actor person2 = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "E:\\xInne\\dk.jpg");
        assertEquals(person, person2);

    }

    @org.junit.jupiter.api.Test
    void testToString() {
        Actor person = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "E:\\xInne\\dk.jpg");

        String expected = "Actor{name='Jack', surname='Sparrow', age=63}";
        assertEquals(expected, person.toString());
    }

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


    
    
}
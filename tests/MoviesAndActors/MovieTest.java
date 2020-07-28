package MoviesAndActors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MovieTest {

    static Collection<Object[]> stringParams() {
        return Arrays.asList(new Object[][]{
                {"Most szpiegów", LocalDate.of(2015, 10, 16)},
                {"Birdman", LocalDate.of(2014, 8, 27)},
                {"Władca pierścieni: Drużyna Pierścienia", LocalDate.of(2001, 12, 13)},
                {"Deadpool", LocalDate.of(2016, 1, 21)},
                {"Kiler", LocalDate.of(1997, 11, 17)}
        });
    }

    Actor actor;
    Actor actor2;
    Actor actor3;
    void createActors() {
        actor = new Actor("Jack", "Sparrow", "Karaibian", "1957-06-02", "E:\\xInne\\dk.jpg");
        actor2 = new Actor("Jackie", "Sparrow", "Karaibian", "1957-06-02", "E:\\xInne\\dk.jpg");
        actor3 = new Actor("John", "Wick", "USA", "1983-07-21", "E:\\xInne\\dk.jpg");
    }

    Movie movie;
    void setUp() {
        movie = new Movie("Most szpiegów", LocalDate.of(2015, 10, 16));
    }


    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getTitle(String title, LocalDate premiere) {
        Movie movie = new Movie(title, premiere);
        assertEquals(title, movie.getTitle());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getPremiere(String title, LocalDate premiere) {
        Movie movie = new Movie(title, premiere);
        assertEquals(premiere, movie.getPremiere());
    }

    @Test
    void convertStrToLocalDate() {
        String str = "1998-04-11";
        assertEquals(LocalDate.of(1998, 4, 11), Movie.convertStrToLocalDate(str));
    }

    @Test
    void setLength() {
        setUp();
        movie.setLength(100);
        assertEquals(100, movie.getLength());
    }

    @Test
    void changeLenStrFromIMDBToInt() {
        String longPattern = "PT1H50M";
        String hourPattern = "PT2H";
        String shortPattern = "PT49M";
        assertEquals(110, Movie.changeLenStrFromIMDBToInt(longPattern));
        assertEquals(120, Movie.changeLenStrFromIMDBToInt(hourPattern));
        assertEquals(49, Movie.changeLenStrFromIMDBToInt(shortPattern));
    }

    @Test
    void setRate() {
        setUp();
        movie.setRate(7.87);
        assertEquals(7.87, movie.getRate());
        setUp();
        movie.setRate(12);
        assertEquals(0, movie.getRate());
        setUp();
        movie.setRate(-1);
        assertEquals(0, movie.getRate());
    }

    @Test
    void setRateCount() {
        setUp();
        movie.setRateCount(24520);
        assertEquals(24520, movie.getRateCount());
        movie.setRateCount(-20);
        assertEquals(24520, movie.getRateCount());
    }

    @Test
    void setTitleOrg() {
        setUp();
        movie.setTitleOrg("Lost Heaven");
        assertEquals("Lost Heaven", movie.getTitleOrg());
    }

    @Test
    void setDescription() {
        setUp();
        movie.setDescription("This is a good movie");
        assertEquals("This is a good movie", movie.getDescription());
    }

    @Test
    void setCoverPath() {
        setUp();
        movie.setCoverPath("E:\\xInne\\dk.jpg");
        assertEquals("E:\\xInne\\dk.jpg", movie.getCoverPath());
    }

    @Test
    void addGenre() {
        setUp();
        movie.addGenre("Akcja");
        List<String> tab = Collections.singletonList("Akcja");
        assertEquals(tab, movie.getGenres());

    }

    @Test
    void addGenres() {
        setUp();
        List<String> tab = Arrays.asList("Akcja", "Thriller");
        movie.addGenres(tab);
        assertEquals(tab, movie.getGenres());
    }

    @Test
    void addActor() {
        setUp();
        createActors();
        movie.addActor(actor);
        List<Actor> tab = Collections.singletonList(actor);
        assertEquals(tab, movie.getCast());
    }

    @Test
    void addActors() {
        setUp();
        createActors();
        List<Actor> tab = Arrays.asList(actor, actor2, actor3);
        movie.addActors(tab);
        assertEquals(tab, movie.getCast());
    }

    @Test
    void addDirector() {
        setUp();
        createActors();
        movie.addDirector(actor);
        List<Actor> tab = Collections.singletonList(actor);
        assertEquals(tab, movie.getDirectors());
    }

    @Test
    void addDirectors() {
        setUp();
        createActors();
        List<Actor> tab = Arrays.asList(actor, actor2, actor3);
        movie.addDirectors(tab);
        assertEquals(tab, movie.getDirectors());
    }

    @Test
    void addWriter() {
        setUp();
        createActors();
        movie.addWriter(actor);
        List<Actor> tab = Collections.singletonList(actor);
        assertEquals(tab, movie.getWriters());
    }

    @Test
    void addWriters() {
        setUp();
        createActors();
        List<Actor> tab = Arrays.asList(actor, actor2, actor3);
        movie.addWriters(tab);
        assertEquals(tab, movie.getWriters());
    }

    @Test
    void isActorPlayingIn() {
        setUp();
        createActors();
        movie.addActor(actor);
        assertTrue(movie.isActorPlayingIn(actor));
        assertFalse(movie.isActorPlayingIn(actor2));
    }

    @Test
    void isDirectedBy() {
        setUp();
        createActors();
        movie.addDirector(actor);
        assertTrue(movie.isDirectedBy(actor));
        assertFalse(movie.isDirectedBy(actor2));
    }

    @Test
    void isWrittenBy() {
        setUp();
        createActors();
        movie.addWriter(actor);
        assertTrue(movie.isWrittenBy(actor));
        assertFalse(movie.isWrittenBy(actor2));
    }

    @Test
    void isGenreType() {
        setUp();
        movie.addGenre("Sci-Fi");
        assertTrue(movie.isGenreType("Sci-Fi"));
        assertFalse(movie.isGenreType("Fantasy"));
    }

    @Test
    void isRateHigherThen() {
        setUp();
        movie.setRate(8.4);
        assertTrue(movie.isRateHigherThen(8));
        assertFalse(movie.isRateHigherThen(9));
    }


    @Test
    void getLengthFormatted() {
        setUp();
        movie.setLength(100);
        assertEquals("1h 40min", movie.getLengthFormatted());
    }


    @Test
    void searchFor() {
        setUp();
        assertTrue(movie.searchFor("most"));
        assertFalse(movie.searchFor("pool"));
    }

    @Test
    void testToString() {
        setUp();
        assertEquals("Movie{title='Most szpiegów', premiere=2015-10-16}", movie.toString());
    }

    @Test
    void compareTo() {
        setUp();
        Movie movie2 = new Movie("Most szpiegów", LocalDate.of(2015, 10, 16));
        Movie movie3 = new Movie("Deadpool", LocalDate.of(2015, 10, 16));
        assertEquals(0, movie.compareTo(movie2));
        assertEquals(9, movie.compareTo(movie3));
    }
}
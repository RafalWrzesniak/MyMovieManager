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
                {Map.ofEntries(
                        Map.entry(Movie.TITLE, Collections.singletonList("Most szpiegów")),
                        Map.entry(Movie.PREMIERE, Collections.singletonList("2015-10-16"))
                )},
                {Map.ofEntries(
                        Map.entry(Movie.TITLE, Collections.singletonList("Birdman")),
                        Map.entry(Movie.PREMIERE, Collections.singletonList("2001-12-13"))
                )},
                {Map.ofEntries(
                        Map.entry(Movie.TITLE, Collections.singletonList("Władca pierścieni: Drużyna Pierścienia")),
                        Map.entry(Movie.PREMIERE, Collections.singletonList("2001-12-13"))
                )},
                {Map.ofEntries(
                        Map.entry(Movie.TITLE, Collections.singletonList("Deadpool")),
                        Map.entry(Movie.PREMIERE, Collections.singletonList("2016-01-21"))
                )},
                {Map.ofEntries(
                        Map.entry(Movie.TITLE, Collections.singletonList("Kiler")),
                        Map.entry(Movie.PREMIERE, Collections.singletonList("1997-11-17"))
                )},
        });
    }

    Actor actor;
    Actor actor2;
    Actor actor3;
    void createActors() {
        actor = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Cezary"),
                Map.entry(Actor.SURNAME, "Pazura"),
                Map.entry(Actor.NATIONALITY, "Poland"),
                Map.entry(Actor.BIRTHDAY, "1962-06-13"),
                Map.entry(Actor.FILMWEB, "www.filmweb.pl"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\cp.jpg")
        ));
        actor2 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "Jack"),
                Map.entry(Actor.SURNAME, "Sparrow"),
                Map.entry(Actor.NATIONALITY, "Karaibian"),
                Map.entry(Actor.BIRTHDAY, "1957-06-02"),
                Map.entry(Actor.FILMWEB, "www.filmweb.pl"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\js.jpg")
        ));
       actor3 = new Actor(Map.ofEntries(
                Map.entry(Actor.NAME, "John"),
                Map.entry(Actor.SURNAME, "Wick"),
                Map.entry(Actor.NATIONALITY, "USA"),
                Map.entry(Actor.BIRTHDAY, "1983-07-21"),
                Map.entry(Actor.FILMWEB, "www.filmweb.pl"),
                Map.entry(Actor.IMAGE_PATH, "E:\\xInne\\jw.jpg")
        ));
    }

    Movie movie;
    void setUp() {
        movie = new Movie(Map.ofEntries(
                Map.entry(Movie.TITLE, Collections.singletonList("Most szpiegów")),
                Map.entry(Movie.PREMIERE, Collections.singletonList("2015-10-16"))
        ));
    }


    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getTitle(Map<String, List<String>> map) {
        Movie movie = new Movie(map);
        assertEquals(map.get(Movie.TITLE).get(0), movie.getTitle());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getPremiere(Map<String, List<String>> map) {
        Movie movie = new Movie(map);
        assertEquals(map.get(Movie.PREMIERE).get(0), movie.getPremiere().toString());
    }

    @Test
    void convertStrToLocalDate() {
        String str = "1998-04-11";
        assertEquals(LocalDate.of(1998, 4, 11), Movie.convertStrToLocalDate(str));
    }

    @Test
    void setLength() {
        setUp();
        movie.setDuration(100);
        assertEquals(100, movie.getDuration());
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
        movie.setImagePath("E:\\xInne\\dk.jpg");
        assertEquals("E:\\xInne\\dk.jpg", movie.getImagePath());
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
        movie.setDuration(100);
        assertEquals("1h 40min", movie.getLengthFormatted());
    }


    @Test
    void searchFor() {
        setUp();
        assertTrue(movie.searchFor("most"));
        assertFalse(movie.searchFor("pool"));
    }


    @Test
    void compareTo() {
        setUp();
        Movie movie2 = new Movie(Map.ofEntries(
                Map.entry(Movie.TITLE, Collections.singletonList("Most szpiegów")),
                Map.entry(Movie.PREMIERE, Collections.singletonList("2015-10-16"))
        ));
        Movie movie3 = new Movie(Map.ofEntries(
                Map.entry(Movie.TITLE, Collections.singletonList("Deadpool")),
                Map.entry(Movie.PREMIERE, Collections.singletonList("2016-01-21"))
        ));
        assertEquals(0, movie.compareTo(movie2));
        assertEquals(9, movie.compareTo(movie3));
    }
}
package MoviesAndActors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    Actor actor;
    Movie most = new Movie("Most szpiegów", LocalDate.of(2015, 10, 16));
    Movie birdman = new Movie("Birdman", LocalDate.of(2014, 8, 27));
    Movie lotr = new Movie("Władca pierścieni: Drużyna Pierścienia", LocalDate.of(2001, 12, 13));
    Movie deadpool = new Movie("Deadpool", LocalDate.of(2016, 1, 21));

    @BeforeEach
    void setUp() {
        actor = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
    }

    @Test
    void getAllMoviesActorPlayedIn() {
        actor.addMovieActorPlayedIn(most);
        actor.addMovieActorPlayedIn(birdman);
        actor.addMovieActorPlayedIn(lotr);
        List<Movie> tab = Arrays.asList(most, birdman, lotr);
        assertArrayEquals(tab.toArray(), actor.getAllMoviesActorPlayedIn().toArray());
    }


    @Test
    void isPlayingIn() {
        actor.addMovieActorPlayedIn(lotr);
        assertTrue(actor.isPlayingIn(lotr));
        assertFalse(actor.isPlayingIn(deadpool));
    }


    @Test
    void addMovieActorPlayedIn() {
        actor.addMovieActorPlayedIn(deadpool);
        assertTrue(actor.isPlayingIn(deadpool));
    }

    @Test
    void addSeveralMoviesToActor() {
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
}
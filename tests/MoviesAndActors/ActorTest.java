package MoviesAndActors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    Actor actor;

    @BeforeEach
    void setUp() {
        actor = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
        actor.addMovieActorPlayedIn(300);
        actor.addMovieActorPlayedIn(2);
        actor.addMovieActorPlayedIn(756);
    }

    @Test
    void getAllMoviesActorPlayedIn() {
        List<Integer> tab = Arrays.asList(300, 2, 756);
        assertArrayEquals(tab.toArray(), actor.getAllMoviesActorPlayedIn().toArray());
    }


    @Test
    void isPlayingIn() {
        assertTrue(actor.isPlayingIn(2));
        assertFalse(actor.isPlayingIn(32));
    }


    @Test
    void addMovieActorPlayedIn() {
        actor.addMovieActorPlayedIn(200);
        assertTrue(actor.isPlayingIn(200));
    }

    @Test
    void addSeveralMoviesToActor() {
        List<Integer> tab = Arrays.asList(100, 120, 140);
        actor.addSeveralMoviesToActor(tab);
        assertTrue(actor.isPlayingIn(100));
        assertTrue(actor.isPlayingIn(120));
        assertTrue(actor.isPlayingIn(140));
        assertFalse(actor.isPlayingIn(240));
    }
}
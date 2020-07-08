package MoviesAndActors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectorTest {

    Director director;

    @BeforeEach
    void setUp() {
        director = new Director("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
        director.addMovieDirectedBy(300);
        director.addMovieDirectedBy(2);
        director.addMovieDirectedBy(756);
    }
    @Test
    void getAllMoviesDirectedBy() {
        List<Integer> tab = Arrays.asList(300, 2, 756);
        assertArrayEquals(tab.toArray(), director.getAllMoviesDirectedBy().toArray());
    }

    @Test
    void isDirecting() {
        assertTrue(director.isDirecting(300));
        assertFalse(director.isDirecting(120));
    }

    @Test
    void addMovieDirectedBy() {
        director.addMovieDirectedBy(100);
        assertTrue(director.isDirecting(100));
    }
}
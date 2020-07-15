package MoviesAndActors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectorTest {

    Director director;
    Movie most = new Movie("Most szpiegów", LocalDate.of(2015, 10, 16));
    Movie birdman = new Movie("Birdman", LocalDate.of(2014, 8, 27));
    Movie lotr = new Movie("Władca pierścieni: Drużyna Pierścienia", LocalDate.of(2001, 12, 13));
    Movie deadpool = new Movie("Deadpool", LocalDate.of(2016, 1, 21));


    @BeforeEach
    void setUp() {
        director = new Director("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
    }

    @Test
    void getAllMoviesDirectedBy() {
        director.addMovieDirectedBy(most);
        director.addMovieDirectedBy(birdman);
        director.addMovieDirectedBy(lotr);
        List<Movie> tab = Arrays.asList(most, birdman, lotr);
        assertArrayEquals(tab.toArray(), director.getAllMoviesDirectedBy().toArray());
    }

    @Test
    void isDirecting() {
        director.addMovieDirectedBy(most);
        assertTrue(director.isDirecting(most));
        assertFalse(director.isDirecting(birdman));
    }

    @Test
    void addMovieDirectedBy() {
        director.addMovieDirectedBy(deadpool);
        assertTrue(director.isDirecting(deadpool));
    }
}
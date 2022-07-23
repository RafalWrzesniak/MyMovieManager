package Internet;

import MoviesAndActors.Movie;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmwebClientMovieTest {

    private final FilmwebClientMovie filmwebClientMovie = FilmwebClientMovie.getInstance();
    private static URL movieUrl;


    @BeforeAll
    @SneakyThrows
    static void setUp() {
        movieUrl = new URL("https://www.filmweb.pl/film/Turysta-2010-484295");
    }

    @Test
    @SneakyThrows
    void createMovieFromUrl() {
        Movie movieTesting = filmwebClientMovie.createMovieFromUrl(movieUrl);

        assertEquals("Turysta", movieTesting.getTitle());
        assertEquals("The Tourist", movieTesting.getTitleOrg());
        assertEquals(LocalDate.of(2010, 12, 6), movieTesting.getPremiere());
        assertEquals("Remake francuskiego thrillera pt. \"Anthony Zimmer\". Amerykański turysta na wakacjach we Włoszech zostaje wplątany przez agentkę Interpolu w niebezpieczną intrygę.", movieTesting.getDescription());
        assertEquals(movieUrl, movieTesting.getFilmweb());
        assertTrue(movieTesting.getGenres().containsAll(List.of("Romans", "Thriller")));
        assertTrue(movieTesting.getProduction().containsAll(List.of("USA", "Włochy", "Francja")));
        assertEquals(new URL("https://fwcdn.pl/fpo/42/95/484295/7346121.3.jpg"), movieTesting.getImageUrl());

    }

}
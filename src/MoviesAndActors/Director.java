package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Director extends Actor {

    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final List<Movie> directedMovies = new ArrayList<>();

    public Director(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        super(name, surname, nationality, birthday, imagePath);
        logger.info("New director created: {}", this.toString());
    }

    public List<Movie> getAllMoviesDirectedBy() {
        return new ArrayList<>(directedMovies);
    }
    public boolean isDirecting(Movie movie) {
        return directedMovies.contains(movie);
    }

    public boolean addMovieDirectedBy(Movie movie) {
        if(isDirecting(movie)) {
            logger.warn("Movie #{} already exists in: {}}", movie, this.toString());
            return false;
        } else {
            directedMovies.add(movie);
            logger.debug("Movie #{} added to: {}", movie, this.toString());
            return true;
        }
    }


}

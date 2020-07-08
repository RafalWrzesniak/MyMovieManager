package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Director extends Actor {

    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final List<Integer> directedMovies = new ArrayList<>();

    public Director(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        super(name, surname, nationality, birthday, imagePath);
        logger.debug("New director created: {}", this.toString());
    }

    public List<Integer> getAllMoviesDirectedBy() {
        return new ArrayList<>(directedMovies);
    }
    public boolean isDirecting(Integer movieId) {
        return directedMovies.contains(movieId);
    }

    public boolean addMovieDirectedBy(Integer movieId) {
        if(movieId < 0) {
            logger.warn("Wrong movie index: {}", movieId);
            return false;
    }
        if(isDirecting(movieId)) {
            logger.warn("Movie #{} already exists in: {}}", movieId, this.toString());
            return false;
        } else {
            directedMovies.add(movieId);
            logger.debug("Movie #{} added to: {}", movieId, this.toString());
            return true;
        }
    }


}

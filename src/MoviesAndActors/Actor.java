package MoviesAndActors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Actor extends Person {

    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final List<Movie> playedInMovies = new ArrayList<>();

    public Actor(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        super(name, surname, nationality, birthday, imagePath);
        logger.info("New actor created: {}", this.toString());
    }


    public List<Movie> getAllMoviesActorPlayedIn() {
        return new ArrayList<>(playedInMovies);
    }

    public boolean isPlayingIn(Movie movie) {
        return playedInMovies.contains(movie);
    }

    public boolean addMovieActorPlayedIn(Movie movie) {
        if(isPlayingIn(movie)) {
            logger.warn("Movie #{} already exists in: {}}", movie, this.toString());
            return false;
        } else {
            playedInMovies.add(movie);
            logger.debug("Movie #{} added to: {}", movie, this.toString());
            return true;
        }
    }

    public boolean addSeveralMoviesToActor(List<Movie> moviesToAdd) {
        if(moviesToAdd.size() < 1) {
            logger.warn("Empty list added as input to add several movies");
            return false;
        }
        for (Movie movie : moviesToAdd) {
            addMovieActorPlayedIn(movie);
        }
        logger.debug("Several movies added to: {}", this.toString());
        return true;
    }



}

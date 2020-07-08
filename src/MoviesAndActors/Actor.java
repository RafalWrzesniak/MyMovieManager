package MoviesAndActors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Actor extends Person {

    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final List<Integer> playedInMovies = new ArrayList<>();

    public Actor(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        super(name, surname, nationality, birthday, imagePath);
        logger.debug("New actor created: {}", this.toString());
    }


    public List<Integer> getAllMoviesActorPlayedIn() {
        return new ArrayList<>(playedInMovies);
    }

    public boolean isPlayingIn(int movieId) {
        return playedInMovies.contains(movieId);
    }

    public boolean addMovieActorPlayedIn(int movieId) {
        if(movieId < 0) {
            logger.warn("Wrong movie index: {}", movieId);
            return false;
        }
        if(isPlayingIn(movieId)) {
            logger.warn("Movie #{} already exists in: {}}", movieId, this.toString());
            return false;
        } else {
            playedInMovies.add(movieId);
            logger.debug("Movie #{} added to: {}", movieId, this.toString());
            return true;
        }
    }

    public boolean addSeveralMoviesToActor(List<Integer> moviesToAdd) {
        if(moviesToAdd.size() < 1) {
            logger.warn("Empty list added as input to add several movies");
            return false;
        }
        for (Integer integer : moviesToAdd) {
            addMovieActorPlayedIn(integer);
        }
        logger.debug("Several movies added to: {}", this.toString());
        return true;
    }



}

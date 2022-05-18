package Service;

import Errors.MovieNotFoundException;
import FileOperations.XMLOperator;
import Internet.FilmwebClientActor;
import Internet.FilmwebClientMovie;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MovieCreatorService {

    private final FilmwebClientMovie filmwebClientMovie;
    private final FilmwebClientActor filmwebClientActor;
    private final ContentList<Movie> allMovies;
    private final ContentList<Actor> allReadActors;
    private final List<String> allActorsAsStrings;

    @SneakyThrows
    public Movie createMovieFromUrl(URL movieUrl) throws MovieNotFoundException {
        Movie movie = null;
        for (int i = 0; movie == null && i < 5; i++) {
            try {
                movie = filmwebClientMovie.createMovieFromUrl(movieUrl);
            } catch (IOException e) {
                log.warn("Retrying to download movie from {}", movieUrl);
                Thread.sleep(1000);
            }
        }
        if(movie == null) throw new MovieNotFoundException("Cannot download movie after 5 retries from " + movieUrl);
        addAllCastToMovie(movie, movieUrl);
        return movie;
    }

    private void addAllCastToMovie(Movie movie, URL movieUrl) {
        addCastToMovie(movie, movieUrl);
        addDirectorsToMovie(movie, movieUrl);
        addWritersToMovie(movie, movieUrl);
    }

    private void addCastToMovie(Movie movie, URL movieUrl) {
        List<URL> cast;
        try {
            cast = filmwebClientMovie.getCastLinks(movieUrl);
        } catch (IOException e) {
            log.warn("Could not find cast from link " + movieUrl);
            return;
        }
        movie.addActors(createActorsFromUrls(cast));
    }

    private void addDirectorsToMovie(Movie movie, URL movieUrl) {
        List<URL> directors;
        try {
            directors = filmwebClientMovie.getDirectorLinks(movieUrl);
        } catch (IOException e) {
            log.warn("Could not find directors from link " + movieUrl);
            return;
        }
        movie.addDirectors(createActorsFromUrls(directors));
    }

    private void addWritersToMovie(Movie movie, URL movieUrl) {
        List<URL> writers;
        try {
            writers = filmwebClientMovie.getWritersLinks(movieUrl);
        } catch (IOException e) {
            log.warn("Could not find writers from link " + movieUrl);
            return;
        }
        movie.addWriters(createActorsFromUrls(writers));
    }

    private List<Actor> createActorsFromUrls(List<URL> urls) {
        List<Actor> actorList = new ArrayList<>();
        for (URL url : urls) {
            Actor actor = getActorFromFileIfExists(url);
            if(actor == null) {
                try {
                    actor = filmwebClientActor.createActorFromFilmweb(url);
                    allReadActors.add(actor);
                } catch (IOException e) {
                    log.warn("Could not find actor data from " + url);
                    continue;
                }
            }
            actorList.add(actor);
        }
        return actorList;
    }

    private Actor getActorFromFileIfExists(URL actorUrl) {
        Actor actor = allReadActors.getObjByUrlIfExists(actorUrl);
        if (actor == null) {
            for (String s : allActorsAsStrings) {
                if (s.contains(actorUrl.toString())) {
                    String id = s.split(";")[0];
                    XMLOperator.createActorsAndAssignThemToMovies(List.of(id), allReadActors, allMovies);
                    actor = allReadActors.getObjByUrlIfExists(actorUrl);
                    break;
                }
            }
        }
        return actor;
    }

}

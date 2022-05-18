package MoviesAndActors.builder;

import MoviesAndActors.Movie;

public class MovieBuilder extends Movie.MovieBuilder {

    @Override
    public Movie build() {
        Movie movie = super.build().withDownloadedLocalImage();
        movie.saveMe();
        return movie;
    }

    public static MovieBuilder builder() {
        return new MovieBuilder();
    }

    public Movie.MovieBuilder createId() {
        return super.createId();
    }

}

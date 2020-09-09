package MyMovieManager;

import FileOperations.IO;
import Internet.Connection;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public final class DownloadAndProcessMovies extends Thread {

        private final List<File> movieFileList;
        private final ContentList<Actor> allActors;
        private final ContentList<Movie> allMovies;

        public DownloadAndProcessMovies(List<File> movieFileList, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
            this.movieFileList = movieFileList;
            this.allMovies = allMovies;
            this.allActors = allActors;
            setName("DownloadAndProcess");
        }

        @Override
        public void run() {
            while (movieFileList.size() != 0) {
                File movieFile;
                synchronized (movieFileList) {
                    movieFile = movieFileList.get(0);
                    movieFileList.remove(0);
                }
                handleMovieFromFile(movieFile, allMovies, allActors);
            }
        }


        public static void handleMovieFromUrl(URL movieUrl, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
            Connection connection;
            Movie movie;
            try {
                connection = new Connection(movieUrl);
                movie = connection.createMovieFromFilmwebLink(allActors);
                allMovies.add(movie);
                movie.printPretty();
                File movieDir = IO.createContentDirectory(movie);
                String downloadedImagePath = movieDir.toString().replaceAll(":", "").concat("\\")
                        .concat(movie.getReprName().concat(".jpg"));
                if( Connection.downloadImage(movie.getImagePath(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                }
            } catch (IOException ignored) { }

        }

        public static void handleMovieFromFile(File movieFile, ContentList<Movie> allMovies, ContentList<Actor> allActors) {
            Connection connection;
            Movie movie;
            try {
                connection = new Connection(movieFile.getName());
                movie = connection.createMovieFromFilmwebLink(allActors);
                allMovies.add(movie);
                movie.printPretty();
                IO.createSummaryImage(movie, movieFile);
                File movieDir = IO.createContentDirectory(movie);
                String downloadedImagePath = movieDir.toString().replaceAll(":", "").concat("\\")
                        .concat(movie.getReprName().concat(".jpg"));
                if( Connection.downloadImage(movie.getImagePath(), downloadedImagePath) ) {
                    movie.setImagePath(downloadedImagePath);
                }
//                return movie;
            } catch (IOException | NullPointerException ignored) { }
//            return null;
        }

}

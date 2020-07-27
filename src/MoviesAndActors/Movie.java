package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class Movie implements ContentType<Movie> {

    private static final Logger logger = LoggerFactory.getLogger(Movie.class.getName());
    private final String title;
    private String titleOrg;
    private String description;
    private final LocalDate premiere;
    private int rateCount;
    private int length;
    private double rate;
    private final List<Actor> cast = new ArrayList<>();
    private final List<Actor> directors = new ArrayList<>();
    private final List<Actor> writers = new ArrayList<>();
    private final List<String> genres = new ArrayList<>();
    private String coverPath;

    public Movie(String title, LocalDate premiere) {
        this.title = title;
        this.premiere = premiere;
        logger.info("New movie \"{}\" created", this.toString());
    }

    public Movie(String title, String premiere) {
        this.title = title;
        this.premiere = convertStrToLocalDate(premiere);
        logger.info("New movie \"{}\" created", this.toString());
    }

    public static LocalDate convertStrToLocalDate(String string) {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null or empty!");
        }
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
    }

    public void setLength(int length) {
        if(this.length != 0) {
            logger.warn("Unsuccessful set of length in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getLengthFormatted());
        } else if(length > 0) {
            this.length = length;
            logger.debug("length of \"{}\" set to \"{}\"", this.toString(), getLengthFormatted());
        }
    }

    public static int changeLenStrFromIMDBToInt(String lenString){
        if(lenString == null || lenString.isEmpty()) {
            return 0;
        }
        DateTimeFormatter longPattern = DateTimeFormatter.ofPattern("'PT'H'H'mm'M'");
        DateTimeFormatter hourPattern = DateTimeFormatter.ofPattern("'PT'H'H'");
        LocalTime time;
        try {
            time = LocalTime.parse(lenString, longPattern);
        } catch (DateTimeParseException e) {
            try {
                time = LocalTime.parse(lenString, hourPattern);
            } catch (DateTimeParseException e2) {
                lenString = lenString.replace("PT", "PT0H");
                time = LocalTime.parse(lenString, longPattern);
            }
        }
        return time.getHour()*60 + time.getMinute();
    }


    public void setRate(double rate) {
        if(this.rate != 0) {
            logger.warn("Unsuccessful set of rate in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getRate());
        } else if(rate > 0 && rate <= 10) {
            this.rate = rate;
            logger.debug("rate of \"{}\" set to \"{}\"", this.toString(), getRate());
        }
    }

    public void setRateCount(int rateCount) {
        if(this.rateCount != 0) {
            logger.warn("Unsuccessful set of rateCount in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getRateCount());
        } else if(rateCount > 0) {
            this.rateCount = rateCount;
            logger.debug("rateCount of \"{}\" set to \"{}\"", this.toString(), getRateCount());
        }
    }

    // strings
    public void setTitleOrg(String titleOrg) {
        if(this.titleOrg != null) {
            logger.warn("Unsuccessful set of titleOrg in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getTitleOrg());
        } else if(!titleOrg.isEmpty()) {
            this.titleOrg = titleOrg;
            logger.debug("titleOrg of \"{}\" set to \"{}\"", this.toString(), getTitleOrg());
        }
    }

    public void setDescription(String description) {
        if(this.description != null) {
            logger.warn("Unsuccessful set of description in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getDescription());
        } else if(!description.isEmpty()) {
            this.description = description;
            logger.debug("description of \"{}\" set to \"{}\"", this.toString(), getDescription());
        }
    }

    public void setCoverPath(String coverPath) {
        if(this.coverPath != null) {
            logger.warn("Unsuccessful set of coverPath in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getCoverPath());
        } else if(!coverPath.isEmpty()) {
            this.coverPath = coverPath;
            logger.debug("coverPath of \"{}\" set to \"{}\"", this.toString(), getCoverPath());
        }
    }


    // genres
    public void addGenre(String genre) {
        if(this.genres.size() > 4 || this.genres.contains(genre)) {
            logger.warn("Unsuccessful set of genre in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getGenres().toString());
        } else if(genre != null && !genre.isEmpty()) {
            this.genres.add(genre);
            logger.debug("genre \"{}\" added to \"{}\"", genre, this.toString());
        }
    }

    public void addGenres(List<String> genres) {
        if(genres.size() > 0 && genres.size() < 4) {
            for (String genre : genres) {
                addGenre(genre);
            }
        }
    }


    // actors
    public void addActor(Actor actor) {
        if(actor == null || this.cast.contains(actor)) {
            logger.warn("Unsuccessful set of actor in movie \"{}\" - this field is already set to \"{}\"", this.toString(), getCast().toString());
        } else {
            this.cast.add(actor);
            logger.debug("actor \"{}\" added to \"{}\"", actor, this.toString());
            actor.addMovieActorPlayedIn(this);
        }
    }

    public void addActors(List<Actor> actors) {
        if(actors.size() > 0) {
            for (Actor actor : actors) {
                addActor(actor);
            }
        }
    }

    // directors
    public void addDirector(Actor director) {
        if(director == null) {
            logger.warn("Unsuccessful set of director in movie \"{}\" - null as an input", this.toString());
        } else {
            this.directors.add(director);
            logger.debug("director of \"{}\" set to \"{}\"", this.toString(), getDirectors().toString());
            director.addMovieDirectedBy(this);
        }
    }

    public void addDirectors(List<Actor> directors) {
        if(directors.size() > 0) {
            for (Actor director: directors) {
                addDirector(director);
            }
        }
    }

    // writers
    public void addWriter(Actor writer) {
        if(writer == null) {
            logger.warn("Unsuccessful set of writer in movie \"{}\" - null as an input", this.toString());
        } else {
            this.writers.add(writer);
            logger.debug("writer of \"{}\" set to \"{}\"", this.toString(), getWriters().toString());
            writer.addMovieWrittenBy(this);
        }
    }

    public void addWriters(List<Actor> writers) {
        if(writers.size() > 0) {
            for (Actor writer: writers) {
                addWriter(writer);
            }
        }
    }


    public boolean isActorPlayingIn(Actor actor) {
        return this.getCast().contains(actor);
    }

    public boolean isDirectedBy(Actor director) {
        return this.getDirectors().contains(director);
    }

    public boolean isWrittenBy(Actor writer) {
        return this.getWriters().contains(writer);
    }

    public boolean isGenreType(String genre) {
        return this.getGenres().contains(genre);
    }

    public boolean isRateHigherThen(double rate) {
        if(rate > 0 && rate <= 10) {
            return this.getRate() > rate;
        } else {
            throw new IllegalArgumentException("Rate must be in range (0, 10]");
        }
    }


    public List<Actor> getWriters() {
        return new ArrayList<>(writers);
    }

    public String getTitle() {
        return title;
    }

    public String getTitleOrg() {
        return titleOrg;
    }

    public String getDescription() {
        return description;
    }

    public List<Actor> getDirectors() {
        return new ArrayList<>(directors);
    }

    public int getRateCount() {
        return rateCount;
    }

    public int getLength() {
        return length;
    }

    public String getLengthFormatted() {
        return String.format("%dh %dmin", length/60, length%60);
    }

    public double getRate() {
        return rate;
    }

    public List<Actor> getCast() {
        return new ArrayList<>(cast);
    }

    public List<String> getGenres() {
        return new ArrayList<>(genres);
    }

    public LocalDate getPremiere() {
        return premiere;
    }

    public String getCoverPath() {
        return coverPath;
    }

    @Override
    public boolean searchFor(String strToFind) {
        String[] strSplit = strToFind.toLowerCase().split(" ");
        for (String searchingStr : strSplit) {
            if (this.getTitle().toLowerCase().contains(searchingStr)) return true;
            try {
                if(this.getDescription().toLowerCase().contains(searchingStr)) return true;
            } catch (NullPointerException ignore) {}
            try {
                if(this.getTitleOrg().toLowerCase().contains(searchingStr)) return true;
            } catch (NullPointerException ignore) {}
            for(String genre : genres) {
                if(genre.toLowerCase().contains(searchingStr)) return true;
            }
//            List<Actor> allMovieActors = new ArrayList<>(cast);
//            allMovieActors.addAll(directors);
//            allMovieActors.addAll(writers);
//            for(Actor actor : allMovieActors) {
//                if(actor.searchFor(strToFind)) return true;
//            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", premiere=" + premiere +
                '}';
    }

    @Override
    public int compareTo(Movie movie) {
        if(movie == null) {
            throw new IllegalArgumentException("Cannot compare to null!");
        }
        if(rate != 0 && movie.getRate() != 0) {
            return (int) (this.getRate() - movie.getRate()) * 100;
        }
        return title.compareToIgnoreCase(movie.getTitle());
    }

}

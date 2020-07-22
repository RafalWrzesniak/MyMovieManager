package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class Movie {

    private static final Logger logger = LoggerFactory.getLogger(Movie.class.getName());
    private final String title;
    private String titleOrg;
    private String description;
    private final LocalDate premiere;
    private Director director;
    private int rateCount;
    private int length;
    private double rate;
    private final List<Actor> cast;
    private final List<String> genres;
    private String coverPath;
    // writers

    public Movie(String title, LocalDate premiere) {
        this.title = title;
        this.premiere = premiere;
        cast = new ArrayList<>();
        genres = new ArrayList<>();
        logger.info("New movie \"{}\" created", getTitle());
    }

    public void setLength(int length) {
        if(this.length != 0) {
            logger.warn("Unsuccessful set of length in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getLengthFormatted());
        } else if(length > 0) {
            this.length = length;
            logger.debug("length of \"{}\" set to \"{}\"", getTitle(), getLengthFormatted());
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
            logger.warn("Unsuccessful set of rate in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getRate());
        } else if(rate > 0 && rate <= 10) {
            this.rate = rate;
            logger.debug("rate of \"{}\" set to \"{}\"", getTitle(), getRate());
        }
    }

    public void setRateCount(int rateCount) {
        if(this.rateCount != 0) {
            logger.warn("Unsuccessful set of rateCount in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getRateCount());
        } else if(rateCount > 0) {
            this.rateCount = rateCount;
            logger.debug("rateCount of \"{}\" set to \"{}\"", getTitle(), getRateCount());
        }
    }

    public void setDirector(Director director) {
        if(this.director != null) {
            logger.warn("Unsuccessful set of director in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getDirector());
        } else {
            this.director = director;
            logger.debug("director of \"{}\" set to \"{}\"", getTitle(), getDirector());
        }
    }

    // strings
    public void setTitleOrg(String titleOrg) {
        if(this.titleOrg != null) {
            logger.warn("Unsuccessful set of titleOrg in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getTitleOrg());
        } else if(!titleOrg.isEmpty()) {
            this.titleOrg = titleOrg;
            logger.debug("titleOrg of \"{}\" set to \"{}\"", getTitle(), getTitleOrg());
        }
    }

    public void setDescription(String description) {
        if(this.description != null) {
            logger.warn("Unsuccessful set of description in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getDescription());
        } else if(!description.isEmpty()) {
            this.description = description;
            logger.debug("description of \"{}\" set to \"{}\"", getTitle(), getDescription());
        }
    }

    public void setCoverPath(String coverPath) {
        if(this.coverPath != null) {
            logger.warn("Unsuccessful set of coverPath in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getCoverPath());
        } else if(!coverPath.isEmpty()) {
            this.coverPath = coverPath;
            logger.debug("coverPath of \"{}\" set to \"{}\"", getTitle(), getCoverPath());
        }
    }

    public void addGenre(String genre) {
        if(this.genres.size() > 4 || this.genres.contains(genre)) {
            logger.warn("Unsuccessful set of genre in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getGenres().toString());
        } else if(genre != null && !genre.isEmpty()) {
            this.genres.add(genre);
            logger.debug("genre \"{}\" added to \"{}\"", genre, getTitle());
        }
    }

    public void addGenres(List<String> genres) {
        if(genres.size() > 0 && genres.size() < 4) {
            for (String genre : genres) {
                addGenre(genre);
            }
        }
    }

    public void addActor(Actor actor) {
        if(actor == null || this.cast.contains(actor)) {
            logger.warn("Unsuccessful set of actor in movie \"{}\" - this field is already set to \"{}\"", getTitle(), getCast().toString());
        } else {
            this.cast.add(actor);
            logger.debug("actor \"{}\" added to \"{}\"", actor, getTitle());
        }
    }

    public void addActors(List<Actor> actors) {
        if(actors.size() > 0) {
            for (Actor actor : actors) {
                addActor(actor);
            }
        }
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

    public Director getDirector() {
        return director;
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
}

package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Actor {
    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final int personId;
    private final String name;
    private final String surname;
    private final String nationality;
    private final LocalDate birthday;
    private final int age;
    private final String imagePath;
    private static int classId = 0;
    // actor
    private boolean actor = false;
    private final List<Movie> playedInMovies = new ArrayList<>();
    // director
    private boolean director = false;
    private final List<Movie> directedMovies = new ArrayList<>();
    // writer
    private boolean writer = false;
    private final List<Movie> writtenMovies = new ArrayList<>();



    public Actor(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        this.name = checkForNullOrEmptyOrIllegalChar(name, "Name");
        this.surname = checkForNullOrEmptyOrIllegalChar(surname, "Surname");
        this.nationality = checkForNullOrEmptyOrIllegalChar(nationality, "Nationality");
        this.birthday = setBirthday(birthday);
        this.age = setAge();
        this.imagePath = checkForNullOrEmptyOrIllegalChar(imagePath, "ImagePath");
        this.personId = classId;
        classId++;
        logger.info("New actor created: {}", this.toString());
    }

    public boolean isActor() {
        return actor;
    }

    public boolean isDirector() {
        return director;
    }

    public boolean isWriter() {
        return writer;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getNationality() {
        return nationality;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public int getAge() {
        return age;
    }

    public int getPersonId() {
        return personId;
    }

    public String getImagePath() {
        return imagePath;
    }

    private LocalDate setBirthday(LocalDate birthday) {
        if(birthday == null) {
            throw new IllegalArgumentException("Birthday argument cannot be null!");
        } else {
            return birthday;
        }
    }

    private int setAge() {
        return LocalDate.now().minusYears(getBirthday().getYear()).getYear();
    }

    public void setIsAnActor(boolean actor) {
        this.actor = actor;
        logger.debug("\"{}\" is now an actor", this.toString());
    }

    public void setIsADirector(boolean director) {
        this.director = director;
        logger.debug("\"{}\" is now a director", this.toString());
    }

    public void setIsAWriter(boolean writer) {
        this.writer = writer;
        logger.debug("\"{}\" is now a writer", this.toString());
    }

    public static String checkForNullOrEmptyOrIllegalChar(String stringToCheck, String argName) {
        if(stringToCheck == null) {
            throw new IllegalArgumentException(String.format("%s argument cannot be null!", argName));
        } else if(stringToCheck.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s argument cannot be empty!", argName));
        }
        for (char aChar : stringToCheck.toCharArray()) {
            if (((aChar < 65 || (aChar > 90 && aChar < 96) || (aChar > 122 && aChar < 192))
                    && aChar != 20 && aChar != 39 && aChar != 44 && aChar != 46 && aChar != 47 && aChar != 58 && aChar != 92)) {
                throw new IllegalArgumentException(String.format("%s argument contains illegal char: '%s'", argName, aChar));
            }
        }
        return stringToCheck;
    }


    // actor
    public List<Movie> getAllMoviesActorPlayedIn() {
        return new ArrayList<>(playedInMovies);
    }

    public boolean isPlayingIn(Movie movie) {
        return playedInMovies.contains(movie);
    }
    public boolean addMovieActorPlayedIn(Movie movie) {
        if(isPlayingIn(movie)) {
            logger.warn("\"{}\" already exists as an actor in: \"{}\"}", this.toString(), movie);
            return false;
        } else {
            setIsAnActor(true);
            playedInMovies.add(movie);
            logger.debug("\"{}\" is now an actor in: \"{}\"", this.toString(), movie);
            return true;
        }
    }

    public boolean addSeveralMoviesToActor(List<Movie> moviesToAdd) {
        if(moviesToAdd.size() < 1) {
            logger.warn("Empty list added as input to \"addSeveralMoviesToActor\" method");
            return false;
        }
        for (Movie movie : moviesToAdd) {
            addMovieActorPlayedIn(movie);
        }
        return true;
    }

    // director
    public List<Movie> getAllMoviesDirectedBy() {
        return new ArrayList<>(directedMovies);
    }
    public boolean isDirecting(Movie movie) {
        return directedMovies.contains(movie);
    }

    public boolean addMovieDirectedBy(Movie movie) {
        if(isDirecting(movie)) {
            logger.warn("\"{}\" already exists as an director in: \"{}\"}", this.toString(), movie);
            return false;
        } else {
            setIsADirector(true);
            directedMovies.add(movie);
            logger.debug("\"{}\" is now a director in: \"{}\"", this.toString(), movie);
            return true;
        }
    }


    // writer
    public List<Movie> getAllMoviesWrittenBy() {
        return new ArrayList<>(writtenMovies);
    }
    public boolean isWriting(Movie movie) {
        return writtenMovies.contains(movie);
    }

    public void addMovieWrittenBy(Movie movie) {
        if(isWriting(movie)) {
            logger.warn("\"{}\" already exists as a writer in: \"{}\"}", this.toString(), movie);
        } else {
            setIsAWriter(true);
            directedMovies.add(movie);
            logger.debug("\"{}\" is now a writer in: \"{}\"", this.toString(), movie);
        }
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actor)) return false;
        Actor actor = (Actor) o;
        return getName().equals(actor.getName()) &&
                getSurname().equals(actor.getSurname()) &&
                getBirthday().equals(actor.getBirthday());
    }

    @Override
    public String toString() {
        return "Actor{" +
                "name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", age=" + getAge() +
                '}';
    }


}

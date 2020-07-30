package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Actor implements ContentType<Actor> {
    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final int id;
    private final String name;
    private final String surname;
    private final String nationality;
    private LocalDate birthday;
    private int age;
    private final String imagePath;
    private static int classActorId;
    // actor
    private boolean actor = false;
    private final List<Movie> playedInMovies = new ArrayList<>();
    // director
    private boolean director = false;
    private final List<Movie> directedMovies = new ArrayList<>();
    // writer
    private boolean writer = false;
    private final List<Movie> writtenMovies = new ArrayList<>();
    public static final List<String> FIELD_NAMES = new ArrayList<>(List.of(
            "id", "name", "surname", "nationality", "birthday",
            "imagePath", "actor", "director", "writer"));


    static {
        classActorId = 0;
    }

    private Actor(String name, String surname, String nationality, String imagePath, int id) {
        this.name = checkForNullOrEmptyOrIllegalChar(name, "Name");
        this.surname = checkForNullOrEmptyOrIllegalChar(surname, "Surname");
        this.nationality = checkForNullOrEmptyOrIllegalChar(nationality, "Nationality");
        this.imagePath = checkForNullOrEmptyOrIllegalChar(imagePath, "ImagePath");
        if(id == -1) {
            this.id = classActorId;
            classActorId++;
        } else {
            this.id = id;
        }
    }

    public Actor(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        this(name, surname, nationality, imagePath, -1);
        this.birthday = setBirthday(birthday);
        this.age = setAge();
        logger.info("New actor created: {}", this.toString());
    }

    public Actor(String name, String surname, String nationality, String birthday, String imagePath) {
        this(name, surname, nationality, imagePath, -1);
        this.birthday = setBirthday(convertBdStringToLocalDate(birthday));
        this.age = setAge();
        logger.info("New actor created: {}", this.toString());
    }

    public Actor(String name, String surname, String nationality, String birthday, String imagePath, String id) {
        this(name, surname, nationality, imagePath, Integer.parseInt(id));
        this.birthday = setBirthday(convertBdStringToLocalDate(birthday));
        this.age = setAge();
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

    public String getNameAndSurname() {
        return getName().concat(" ").concat(getSurname());
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

    public int getId() {
        return id;
    }


    @Override
    public Map<String, String> getAllFields() {
        Function<List<Movie>, String> getMovieId = movies -> {
        if(movies == null || movies.size() == 0) return null;
        String tmpStr = "";
        for (Movie movie : movies) {
            tmpStr = tmpStr.concat(String.valueOf(movie.getId())).concat(";");
        }
        tmpStr = tmpStr.substring(0, tmpStr.length()-1);
        return tmpStr;
        };

        Map<String, String> map = new LinkedHashMap<>();
        map.put(FIELD_NAMES.get(0), String.valueOf(id));
        map.put(FIELD_NAMES.get(1), name);
        map.put(FIELD_NAMES.get(2), surname);
        map.put(FIELD_NAMES.get(3), nationality);
        map.put(FIELD_NAMES.get(4), getBirthday().toString());
        map.put(FIELD_NAMES.get(5), imagePath);
        map.put(FIELD_NAMES.get(6), getMovieId.apply(playedInMovies));
        map.put(FIELD_NAMES.get(7), getMovieId.apply(directedMovies));
        map.put(FIELD_NAMES.get(8), getMovieId.apply(writtenMovies));
        return map;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static LocalDate convertBdStringToLocalDate(String string) {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Birthday argument cannot be null or empty!");
        }
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
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
            if(!movie.isActorPlayingIn(this)) {
                movie.addActor(this);
            }
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
            if(!movie.isDirectedBy(this)) {
                movie.addDirector(this);
            }
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
            writtenMovies.add(movie);
            if(!movie.isWrittenBy(this)) {
                movie.addWriter(this);
            }
            logger.debug("\"{}\" is now a writer in: \"{}\"", this.toString(), movie);
        }
    }

    @Override
    public boolean searchFor(String strToFind) {
        String[] strSplit = strToFind.toLowerCase().split(" ");
        for (String searchingStr : strSplit) {
            if (this.getNameAndSurname().toLowerCase().contains(searchingStr) ||
                    this.getNationality().toLowerCase().contains(searchingStr)) {
                return true;
            }
//            List<Movie> allActorMovies = new ArrayList<>(playedInMovies);
//            allActorMovies.addAll(directedMovies);
//            allActorMovies.addAll(writtenMovies);
//            for(Movie movie : allActorMovies) {
//                if(movie.searchFor(searchingStr)) return true;
//            }
        }
        return false;
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


    @Override
    public int compareTo(Actor actor) {
        if(actor == null) {
            throw new IllegalArgumentException("Cannot compare to null!");
        }
        return this.getNameAndSurname().compareTo(actor.getNameAndSurname());
    }



}

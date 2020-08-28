package MoviesAndActors;

import FileOperations.IO;
import FileOperations.XMLOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Actor implements ContentType<Actor> {
    private static final Logger logger = LoggerFactory.getLogger(Actor.class.getName());
    private final int id;
    private final String name;
    private final String surname;
    private final String nationality;
    private LocalDate birthday;
    private LocalDate deathDay;
    private int age;
    private final String imagePath;
    private final String filmweb;
    private static int classActorId;
    // actor
    private boolean isActor = false;
    private final List<Movie> playedInMovies = new ArrayList<>();
    // director
    private boolean isDirector = false;
    private final List<Movie> directedMovies = new ArrayList<>();
    // writer
    private boolean isWriter = false;
    private final List<Movie> wroteMovies = new ArrayList<>();

    public static final String NAME = "name", SURNAME = "surname", NATIONALITY = "nationality", BIRTHDAY = "birthday",
            PLAYED_IN_MOVIES = "playedInMovies", DIRECTED_MOVIES = "directedMovies", WROTE_MOVIES = "writtenMovies",
            DEATH_DAY = "deathDay";
    public static final List<String> FIELD_NAMES = new ArrayList<>(List.of(ID, NAME, SURNAME, NATIONALITY, BIRTHDAY,
            DEATH_DAY, IMAGE_PATH, FILMWEB, PLAYED_IN_MOVIES, DIRECTED_MOVIES, WROTE_MOVIES));

    static {
        updateClassActorId();
    }

    public static void updateClassActorId() {
        File actorDir = new File(XMLOperator.getSavePathActor());
        List<String> files = IO.getFileNamesInDirectory(actorDir);
        if(files.size() == 0) {
            classActorId = 0;
        } else {
            for (String name : files) {
                Pattern pattern = Pattern.compile("^actor(\\d+)$");
                Matcher matcher = pattern.matcher(name);
                if (matcher.find() && Integer.parseInt(matcher.group(1)) >= classActorId){
                    classActorId = Integer.parseInt(matcher.group(1));
                    classActorId++;
                }
            }
        }
    }

    public Actor(Map<String, String> actorMap) {
        this(actorMap.get(NAME), actorMap.get(SURNAME), actorMap.get(NATIONALITY), actorMap.get(IMAGE_PATH),
                actorMap.get(FILMWEB), actorMap.get(ID) == null ? -1 : Integer.parseInt(actorMap.get(ID)));
        this.birthday = setBirthday(convertBdStringToLocalDate(actorMap.get(Actor.BIRTHDAY)));
        if(actorMap.get(Actor.DEATH_DAY) != null) {
            setDeathDay(LocalDate.parse(actorMap.get(Actor.DEATH_DAY)));
        }
        setAge();
        logger.info("New actor created: {}", this.toString());
        saveMe();
    }

    private Actor(String name, String surname, String nationality, String imagePath, String filmweb, int id) {
        updateClassActorId();
        this.name = ContentType.checkForNullOrEmptyOrIllegalChar(name, "Name");
        this.surname = ContentType.checkForNullOrEmptyOrIllegalChar(surname, "Surname");
        this.nationality = ContentType.checkForNullOrEmptyOrIllegalChar(nationality, "Nationality");
        this.imagePath = ContentType.checkForNullOrEmptyOrIllegalChar(imagePath, "imagePath");
        this.filmweb = ContentType.checkForNullOrEmptyOrIllegalChar(filmweb, "filmweb");
        if(id == -1) {
            this.id = classActorId;
            classActorId++;
        } else {
            this.id = id;
        }
    }

    public Actor(String name, String surname, String nationality, LocalDate birthday, String imagePath, String filmweb) {
        this(name, surname, nationality, imagePath, filmweb, -1);
        this.birthday = setBirthday(birthday);
        setAge();
        logger.info("New actor created: {}", this.toString());
        saveMe();
    }

    public Actor(String name, String surname, String nationality, String birthday, String imagePath, String filmweb) {
        this(name, surname, nationality, imagePath, filmweb, -1);
        this.birthday = setBirthday(convertBdStringToLocalDate(birthday));
        setAge();
        logger.info("New actor created: {}", this.toString());
        saveMe();
    }

    public Actor(String name, String surname, String nationality, String birthday, String imagePath, String filmweb, String id) {
        this(name, surname, nationality, imagePath, filmweb, Integer.parseInt(id));
        this.birthday = setBirthday(convertBdStringToLocalDate(birthday));
        setAge();
    }


    public boolean isActor() {
        return isActor;
    }

    public boolean isDirector() {
        return isDirector;
    }

    public boolean isWriter() {
        return isWriter;
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

    public String getFilmweb() {
        return filmweb;
    }

    @Override
    public Map<String, String> getAllFieldsAsStrings() {
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
        map.put(ContentType.ID, String.valueOf(id));
        map.put(NAME, name);
        map.put(SURNAME, surname);
        map.put(NATIONALITY, nationality);
        map.put(BIRTHDAY, getBirthday().toString());
        if(deathDay != null) map.put(DEATH_DAY, deathDay.toString());
        map.put(ContentType.IMAGE_PATH, imagePath);
        map.put(ContentType.FILMWEB, filmweb);
        map.put(PLAYED_IN_MOVIES, getMovieId.apply(playedInMovies));
        map.put(DIRECTED_MOVIES, getMovieId.apply(directedMovies));
        map.put(WROTE_MOVIES, getMovieId.apply(wroteMovies));
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

    private void setAge() {
        if(deathDay == null) {
            this.age = LocalDate.now().minusYears(getBirthday().getYear()).getYear();
            return;
        }
        this.age = deathDay.minusYears(birthday.getYear()).getYear();
    }

    public LocalDate getDeathDay() {
        return deathDay;
    }

    public void setDeathDay(LocalDate deathDay) {
        if(deathDay.isBefore(birthday)) {
            throw new IllegalArgumentException("DeathDay can't be before birthday!");
        }
        this.deathDay = deathDay;
        setAge();
    }

    public void setIsAnActor(boolean actor) {
        if(!this.isActor) {
            this.isActor = actor;
            logger.debug("\"{}\" is now an actor", this.toString());
        }
    }

    public void setIsADirector(boolean director) {
        if(!this.isDirector) {
            this.isDirector = director;
            logger.debug("\"{}\" is now a director", this.toString());
        }
    }

    public void setIsAWriter(boolean writer) {
        if(!this.isWriter) {
            this.isWriter = writer;
            logger.debug("\"{}\" is now a writer", this.toString());
        }
    }

    // actor
    public List<Movie> getAllMoviesActorPlayedIn() {
        return new ArrayList<>(playedInMovies);
    }

    public boolean isPlayingIn(Movie movie) {
        return playedInMovies.contains(movie);
    }
    public void addMovieActorPlayedIn(Movie movie) {
        if(isPlayingIn(movie)) {
            logger.warn("\"{}\" already exists as an actor in: \"{}\"}", this.toString(), movie);
        } else {
            setIsAnActor(true);
            playedInMovies.add(movie);
            if(!movie.isActorPlayingIn(this)) {
                movie.addActor(this);
            }
            logger.debug("\"{}\" is now an actor in: \"{}\"", this.toString(), movie);
            saveMe();
        }
    }

    public void addSeveralMoviesToActor(List<Movie> moviesToAdd) {
        if(moviesToAdd.size() < 1) {
            logger.warn("Empty list added as input to \"addSeveralMoviesToActor\" method");
            return;
        }
        for (Movie movie : moviesToAdd) {
            addMovieActorPlayedIn(movie);
        }
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
            saveMe();
            return true;
        }
    }


    // writer
    public List<Movie> getAllMoviesWrittenBy() {
        return new ArrayList<>(wroteMovies);
    }
    public boolean isWriting(Movie movie) {
        return wroteMovies.contains(movie);
    }

    public void addMovieWrittenBy(Movie movie) {
        if(isWriting(movie)) {
            logger.warn("\"{}\" already exists as a writer in: \"{}\"}", this.toString(), movie);
        } else {
            setIsAWriter(true);
            wroteMovies.add(movie);
            if(!movie.isWrittenBy(this)) {
                movie.addWriter(this);
            }
            logger.debug("\"{}\" is now a writer in: \"{}\"", this.toString(), movie);
            saveMe();
        }
    }

    public void printPretty() {
        System.out.println("ID          : " + id);
        System.out.println("FullName    : " + name + " " + surname);
        System.out.println("BirthDate   : " + birthday);
        if(deathDay != null) System.out.println("DeathDate   : " + deathDay);
        System.out.println("Age         : " + age);
        System.out.println("Nationality : " + nationality);
    }

    @Override
    public String getReprName() {
        return getNameAndSurname().replaceAll(" ", "_");
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
        try {
            return filmweb.equals(strToFind);
        } catch (NullPointerException ignore) {}
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
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
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

    @Override
    public void saveMe() {
        if(!XMLOperator.NEW_OBJECTS.contains(this)) {
            XMLOperator.NEW_OBJECTS.add(this);
            logger.debug("Actor \"{}\" added to the list of new objects", this);
        }
    }



}

package MoviesAndActors;

import FileOperations.AutoSave;
import FileOperations.IO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class Actor implements ContentType<Actor> {

//    == fields ==
    @Getter private final int id;
    @Getter private int age;
    @Getter private String name;
    @Getter private String surname;
    @Getter private String nationality;
    @Getter private LocalDate deathDay;
    @Getter @Setter private LocalDate birthday;
    @Getter @Setter private Path imagePath;
    @Getter @Setter private URL imageUrl;
    @Getter @Setter private URL filmweb;
    // actor
    @Getter private boolean isActor = false;
    private final List<Movie> playedInMovies = new ArrayList<>();
    // director
    @Getter private boolean isDirector = false;
    private final List<Movie> directedMovies = new ArrayList<>();
    // writer
    @Getter private boolean isWriter = false;
    private final List<Movie> wroteMovies = new ArrayList<>();

//    == static fields ==
    private static int classActorId = -1;

//    == constants ==
    public static final String NAME = "name", SURNAME = "surname", NATIONALITY = "nationality", BIRTHDAY = "birthday",
            PLAYED_IN_MOVIES = "playedInMovies", DIRECTED_MOVIES = "directedMovies", WROTE_MOVIES = "writtenMovies",
            DEATH_DAY = "deathDay";
    public static final List<String> FIELD_NAMES = List.of(ID, NAME, SURNAME, NATIONALITY, BIRTHDAY,
            DEATH_DAY, IMAGE_PATH, IMAGE_URL, FILMWEB, PLAYED_IN_MOVIES, DIRECTED_MOVIES, WROTE_MOVIES);

    public static final Comparator<Actor> COMP_ID = Comparator.comparingInt(Actor::getId);
    public static final Comparator<Actor> COMP_AGE = Comparator.comparingInt(Actor::getAge);


//    == constructors ==

    public Actor(Map<String, String> actorMap) {
        updateClassActorId();
        try { setFilmweb(new URL(actorMap.get(FILMWEB)));
        } catch (MalformedURLException ignored) {
            throw new NullPointerException("Can't create actor when filmweb " + actorMap.get(FILMWEB) + " is incorrect");
        }
        setName(actorMap.get(NAME));
        setSurname(actorMap.get(SURNAME));
        setNationality(actorMap.get(NATIONALITY));
        try { this.imagePath = Paths.get(actorMap.get(IMAGE_PATH)); } catch (NullPointerException ignored) { }
        try { this.imageUrl = new URL(actorMap.get(IMAGE_URL)); } catch (MalformedURLException ignored) { }
        this.birthday = convertBdStringToLocalDate(actorMap.get(Actor.BIRTHDAY));
        if(actorMap.get(Actor.DEATH_DAY) != null) {
            setDeathDay(LocalDate.parse(actorMap.get(Actor.DEATH_DAY)));
        }
        setAge();
        int id = actorMap.get(ID) == null ? -1 : Integer.parseInt(actorMap.get(ID));
        if(id == -1) {
            this.id = classActorId;
            classActorId++;
            saveMe();
        } else {
            this.id = id;
        }
        log.info("New actor created: {}", this.toString());
    }


//    == getters ==

    public String getNameAndSurname() {
        return getName().concat(" ").concat(getSurname());
    }

    public List<Movie> getAllMoviesActorPlayedIn() {
        return new ArrayList<>(playedInMovies);
    }
    public List<Movie> getAllMoviesDirectedBy() {
        return new ArrayList<>(directedMovies);
    }
    public List<Movie> getAllMoviesWrittenBy() {
        return new ArrayList<>(wroteMovies);
    }

    @Override
    public String getReprName() {
        return getNameAndSurname().replaceAll(" ", "_");
    }


//    == setters ==

    public void setName(String name) {
        this.name = ContentType.checkForNullOrEmptyOrIllegalChar(name, Actor.NAME);
    }

    public void setSurname(String surname) {
        this.surname = ContentType.checkForNullOrEmptyOrIllegalChar(surname, Actor.SURNAME);
    }

    public void setNationality(String nationality) {
        this.nationality = ContentType.checkForNullOrEmptyOrIllegalChar(nationality, Actor.NATIONALITY);
    }

    private void setAge() {
        if(birthday == null) return;
        if(deathDay == null) {
            this.age = LocalDate.now().minusYears(getBirthday().getYear()).getYear();
        } else {
            this.age = deathDay.minusYears(birthday.getYear()).getYear();
        }
    }

    public void setDeathDay(LocalDate deathDay) {
        if(deathDay.isBefore(birthday)) {
            throw new IllegalArgumentException("DeathDay can't be before birthday!");
        }
        this.deathDay = deathDay;
        setAge();
    }

    public void setIsAnActor(boolean isActor) {
        if(!this.isActor) {
            this.isActor = isActor;
            log.debug("\"{}\" is now an actor", this.toString());
        }
    }

    public void setIsADirector(boolean isDirector) {
        if(!this.isDirector) {
            this.isDirector = isDirector;
            log.debug("\"{}\" is now a director", this.toString());
        }
    }

    public void setIsAWriter(boolean isWriter) {
        if(!this.isWriter) {
            this.isWriter = isWriter;
            log.debug("\"{}\" is now a writer", this.toString());
        }
    }


//    == private static methods ==

    private synchronized static void updateClassActorId() {
        File actorDir = IO.getSAVE_PATH_ACTOR().toFile();
        List<String> files = IO.getFileNamesInDirectory(actorDir);
        if(files.size() == 0 && classActorId == -1) {
            classActorId = 0;
        } else if(files.size() != 0){
            for (String name : files) {
                Pattern pattern = Pattern.compile("^actor(\\d+)$");
                Matcher matcher = pattern.matcher(name);
                if (matcher.find() && Integer.parseInt(matcher.group(1)) >= classActorId){
                    classActorId = Integer.parseInt(matcher.group(1));
                    classActorId++;
                }
            }
        }
        if(classActorId == -1) classActorId = 0;
    }

    private static LocalDate convertBdStringToLocalDate(String string) {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Birthday argument cannot be null or empty!");
        } else if(string.equals("-")) return null;
        if(string.matches("^\\d{4}$")) {
            return LocalDate.of(Integer.parseInt(string), 1, 1);
        } else if(string.matches("^\\d{4}-\\d{2}$")) {
            return LocalDate.of(Integer.parseInt(string.substring(0, 4)), Integer.parseInt(string.substring(5)), 1);
        }
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
    }


//    == public methods ==

    // actor
    public boolean isPlayingIn(Movie movie) {
        return playedInMovies.contains(movie);
    }
    public void addMovieActorPlayedIn(Movie movie) {
        if(isPlayingIn(movie)) {
            log.warn("\"{}\" already exists as an actor in: \"{}\"}", this.toString(), movie);
        } else {
            setIsAnActor(true);
            playedInMovies.add(movie);
            if(!movie.isActorPlayingIn(this)) {
                movie.addActor(this);
            }
            log.debug("\"{}\" is now an actor in: \"{}\"", this.toString(), movie);
            saveMe();
        }
    }
    public void addSeveralMoviesToActor(List<Movie> moviesToAdd) {
        if(moviesToAdd.size() < 1) {
            log.warn("Empty list added as input to \"addSeveralMoviesToActor\" method");
            return;
        }
        for (Movie movie : moviesToAdd) {
            addMovieActorPlayedIn(movie);
        }
    }

    // director
    public boolean isDirecting(Movie movie) {
        return directedMovies.contains(movie);
    }
    public void addMovieDirectedBy(Movie movie) {
        if(isDirecting(movie)) {
            log.warn("\"{}\" already exists as an director in: \"{}\"}", this.toString(), movie);
        } else {
            setIsADirector(true);
            directedMovies.add(movie);
            if(!movie.isDirectedBy(this)) {
                movie.addDirector(this);
            }
            log.debug("\"{}\" is now a director in: \"{}\"", this.toString(), movie);
            saveMe();
        }
    }

    // writer
    public boolean isWriting(Movie movie) {
        return wroteMovies.contains(movie);
    }
    public void addMovieWrittenBy(Movie movie) {
        if(isWriting(movie)) {
            log.warn("\"{}\" already exists as a writer in: \"{}\"}", this.toString(), movie);
        } else {
            setIsAWriter(true);
            wroteMovies.add(movie);
            if(!movie.isWrittenBy(this)) {
                movie.addWriter(this);
            }
            log.debug("\"{}\" is now a writer in: \"{}\"", this.toString(), movie);
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
        map.put(ContentType.IMAGE_PATH, imagePath != null ? imagePath.toString() : null);
        map.put(ContentType.IMAGE_URL, imageUrl != null ? imageUrl.toString() : null);
        map.put(ContentType.FILMWEB, filmweb.toString());
        map.put(PLAYED_IN_MOVIES, getMovieId.apply(playedInMovies));
        map.put(DIRECTED_MOVIES, getMovieId.apply(directedMovies));
        map.put(WROTE_MOVIES, getMovieId.apply(wroteMovies));
        return map;
    }


    @Override
    public boolean searchFor(String strToFind) {
        String[] strSplit = strToFind.toLowerCase().split(" ");
        for (String searchingStr : strSplit) {
            if (this.getNameAndSurname().toLowerCase().contains(searchingStr) ||
                    this.getNationality().toLowerCase().contains(searchingStr)) {
                return true;
            }
        }
        try {
            return filmweb.toString().equals(strToFind);
        } catch (NullPointerException ignore) {}
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actor)) return false;
        Actor actor = (Actor) o;
        return filmweb.equals(actor.getFilmweb());
    }

    @Override
    public String toString() {
        String bold = "\033[1m";
        String end = "\033[0m";
        return "Actor{" +
                "id='" + bold + id + end + '\'' +
                ", name='" + bold + name + end + '\'' +
                ", surname='" + bold + surname + end + '\'' +
                ", age='" + bold + age + end + '\'' +
                ", born='" + bold + nationality + end + '\'' +
        '}';
    }

    @Override
    public int compareTo(Actor actor) {
        if(actor == null) {
            throw new IllegalArgumentException("Cannot compare to null!");
        }
        return getReprName().toLowerCase().compareTo(actor.getReprName().toLowerCase());
    }

    @Override
    public void saveMe() {
        synchronized (AutoSave.NEW_OBJECTS) {
            if(!AutoSave.NEW_OBJECTS.contains(this)) {
                AutoSave.NEW_OBJECTS.add(this);
                AutoSave.NEW_OBJECTS.notify();
                log.debug("Actor \"{}\" added to the list of new objects", this);
            }
        }
    }

}

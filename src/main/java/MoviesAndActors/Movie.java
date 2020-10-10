package MoviesAndActors;

import Configuration.Config;
import FileOperations.AutoSave;
import FileOperations.IO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public final class Movie implements ContentType<Movie> {

//   == fields ==
    private int id;
    private int duration;
    private int rateCount;
    private double rate;
    private String title;
    private String titleOrg;
    private String description;
    private LocalDate premiere;
    private Path imagePath;
    private URL imageUrl;
    @Setter private URL filmweb;
    private List<Actor> cast = new ArrayList<>();
    private List<Actor> directors = new ArrayList<>();
    private List<Actor> writers = new ArrayList<>();
    private List<String> genres = new ArrayList<>();
    private List<String> production = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private static int classMovieId = -1;
    @Getter(AccessLevel.NONE)
    private boolean iAmFromConstructor = false;

//    == constants ==
    @Getter(AccessLevel.NONE)
    public static String TITLE = "title", TITLE_ORG = "titleOrg",  PREMIERE = "premiere", DURATION = "duration",
            RATE = "rate", RATE_COUNT = "rateCount", CAST = "cast", DIRECTORS = "directors", WRITERS = "writers",
            GENRES = "genres", PRODUCTION = "production", DESCRIPTION = "description";
    @Getter(AccessLevel.NONE)
    public static final List<String> FIELD_NAMES = List.of(ContentType.ID, TITLE, TITLE_ORG, PREMIERE, DURATION,
            RATE, RATE_COUNT, CAST, DIRECTORS, WRITERS, GENRES, PRODUCTION, DESCRIPTION, IMAGE_PATH, IMAGE_URL, FILMWEB);

    @Getter(AccessLevel.NONE)
    public static final Comparator<Movie> COMP_ID = Comparator.comparingInt(Movie::getId);
    @Getter(AccessLevel.NONE)
    public static final Comparator<Movie> COMP_DURATION = Comparator.comparingInt(Movie::getDuration);
    @Getter(AccessLevel.NONE)
    public static final Comparator<Movie> COMP_PREMIERE = Comparator.comparing(Movie::getPremiere);
    @Getter(AccessLevel.NONE)
    public static final Comparator<Movie> COMP_RATE = Comparator.comparingDouble(Movie::getRate);


//    == constructors ==

    public Movie(Map<String, List<String>> fieldMap, ContentList<Actor> allActors) {
        updateClassMovieId();
        iAmFromConstructor = true;
        try {
            setFilmweb(new URL(fieldMap.get(Movie.FILMWEB).get(0)));
        } catch (MalformedURLException ignored) {
            throw new NullPointerException("Can't create movie when filmweb is incorrect");
        }
        for(String field : fieldMap.keySet()) {
            setFieldWithList(field, fieldMap.get(field));
        }
        if(id == -1) {
            id = classMovieId;
            classMovieId++;
        }
        log.info("New movie \"{}\" created", this.toString());
        iAmFromConstructor = false;
        setFieldWithList("cast", allActors.convertStrIdsToObjects(fieldMap.get("cast")));
        setFieldWithList("directors", allActors.convertStrIdsToObjects(fieldMap.get("directors")));
        setFieldWithList("writers", allActors.convertStrIdsToObjects(fieldMap.get("writers")));
    }

    public Movie(Map<String, List<String>> fieldMap) {
        updateClassMovieId();
        iAmFromConstructor = true;
        try {
            setFilmweb(new URL(fieldMap.get(Movie.FILMWEB).get(0)));
        } catch (MalformedURLException ignored) {
            throw new NullPointerException("Can't create movie when filmweb is incorrect");
        }
        for(String field : fieldMap.keySet()) {
            setFieldWithList(field, fieldMap.get(field));
        }
        int id = fieldMap.get(ID) == null ? -1 : Integer.parseInt(fieldMap.get(ID).get(0));
        if(id == -1) {
            this.id = classMovieId;
            classMovieId++;
        }
        log.info("New movie \"{}\" created", this.toString());
        iAmFromConstructor = false;
        saveMe();
    }


//   == private static methods ==

    private synchronized static void updateClassMovieId() {
        File movieDir = Config.getSAVE_PATH_MOVIE().toFile();
        List<String> files = IO.getFileNamesInDirectory(movieDir);
        if(files.size() == 0 && classMovieId == -1) {
            classMovieId = 0;
        } else if(files.size() != 0){
            for (String name : files) {
                Pattern pattern = Pattern.compile("^movie(\\d+)$");
                Matcher matcher = pattern.matcher(name);
                if (matcher.find() && Integer.parseInt(matcher.group(1)) >= classMovieId) {
                    classMovieId = Integer.parseInt(matcher.group(1));
                    classMovieId++;
                }
            }
        }
        if(classMovieId == -1) classMovieId = 0;
    }


//  == setters ==

    private <E> void setFieldString(String field, E value) {
        try { //check if value is int or double
            setFieldDigit(field, Integer.parseInt((String) value));
            return;
        } catch (NumberFormatException e) {
//            e.printStackTrace();
            try {
                setFieldDigit(field, Double.parseDouble((String) value));
                return;
            } catch (NumberFormatException d) {
//                d.printStackTrace();
            }
        }

        try {
            if (Movie.class.getDeclaredField(field).toString().contains("java.time.LocalDate")) {
                Movie.class.getDeclaredField(field).set(this, convertStrToLocalDate(String.valueOf(value)));
            }
            else if (Movie.class.getDeclaredField(field).toString().contains("java.net.URL")) {
                Movie.class.getDeclaredField(field).set(this, new URL(String.valueOf(value)));
            }
            else if (Movie.class.getDeclaredField(field).toString().contains("java.nio.file.Path")) {
                Movie.class.getDeclaredField(field).set(this, Paths.get(String.valueOf(value)));
            }
            else {
                Movie.class.getDeclaredField(field).set(this, ContentType.checkForNullOrEmptyOrIllegalChar(String.valueOf(value), field));
            }
            saveMe();
        } catch (NoSuchFieldException | IllegalAccessException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private <E> void setFieldDigit(String field, E value) {
        if (!(value instanceof Integer) && !(value instanceof Double)) return;

        try {
            if(Movie.class.getDeclaredField(field).get(this).toString().equals("0") || Movie.class.getDeclaredField(field).get(this).toString().equals("0.0")) {
                if(field.equals(Movie.RATE)) {
                    double doubleValue = (double) value;
                    if(doubleValue > 0 && doubleValue <= 10) {
                        Movie.class.getDeclaredField(field).set(this, (double) (Math.round(doubleValue * 100)) / 100);
                    } else {
                        log.warn("Unsuccessful set of \"{}\" in movie \"{}\" - rate has to be in range (0,10]", field, this.toString());
                    }
                } else {
                    Movie.class.getDeclaredField(field).set(this, value);
                }
//                logger.debug("Field \"{}\" of \"{}\" set to \"{}\"",  field, this.toString(), Movie.class.getDeclaredField(field).get(this));
                saveMe();
            } else {
                log.warn("Unsuccessful set of \"{}\" in movie \"{}\" - this field is already set", field, this.toString());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <E> void setField(String field, E value) {
        if(value == null) {
            log.warn("Unsuccessful set of \"{}\" in \"{}\" - null as input", field, this);
            return;
        }
        try {
            if(!(Movie.class.getDeclaredField(field).get(this) instanceof List)) {
                setFieldString(field, value);
                return;
            }

            if(value instanceof Actor) {
                @SuppressWarnings("unchecked")
                List<Actor> list = (List<Actor>) Movie.class.getDeclaredField(field).get(this);
                if(!list.contains(value)) {
                    list.add((Actor) value);
                    Movie.class.getDeclaredField(field).set(this, list);
                    switch(field) {
                        case "cast":
                            ((Actor) value).addMovieActorPlayedIn(this);
                            break;
                        case "directors":
                            ((Actor) value).addMovieDirectedBy(this);
                            break;
                        case "writers":
                            ((Actor) value).addMovieWrittenBy(this);
                            break;
                    }
                } else {
                    log.warn("Unsuccessful set of {}. \"{}\" is already on the list", "\"" + field + "\" in " + this, value);
                    return;
                }
            } else if(value instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) Movie.class.getDeclaredField(field).get(this);
                String fieldName = Movie.class.getDeclaredField(field).getName();
                if (list.contains(value)) {
                    log.warn("Unsuccessful set of {}. \"{}\" is already on the list", "\"" + field + "\" in " + this, value);
                    return;
                } else if(fieldName.equals("cast") || fieldName.equals("directors") || fieldName.equals("writers")) return;
                list.add((String) value);
                Movie.class.getDeclaredField(field).set(this, list);
            }
            saveMe();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    private <E> void setFieldWithList(String field, List<E> list) {
        if(list.size() > 0) {
            for (E obj : list) {
                setField(field, obj);
            }
        }
    }


    public void setDuration(int duration) {
        setFieldDigit(DURATION, duration);
    }

    public void setLength(String length) {
        setFieldString(DURATION, length);
    }

    public void setRate(double rate) {
        if(rate > 0 && rate <= 10) {
            setFieldDigit(RATE, rate);
        }
    }

    public void setRateCount(int rateCount) {
        setFieldDigit(RATE_COUNT, rateCount);
    }

    public void setTitleOrg(String titleOrg) {
        setFieldString(TITLE_ORG, titleOrg);
    }

    public void setDescription(String description) {
        setFieldString(DESCRIPTION, description);
    }

    public void setImagePath(Path imagePath) {
        setFieldString(IMAGE_PATH, imagePath.toString());
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    //production
    public void addProduction(String production) {
        setField(PRODUCTION, production);
    }

    public void addProductions(List<String> producers) {
        setFieldWithList(PRODUCTION, producers);
    }


    // genres
    public void addGenre(String genre) {
        setField(GENRES, genre);
    }

    public void addGenres(List<String> genres) {
        setFieldWithList(GENRES, genres);
    }


    // actors
    public void addActor(Actor actor) {
        setField(CAST, actor);
    }

    public void addActors(List<Actor> actors) {
        setFieldWithList(CAST, actors);
    }

    // directors
    public void addDirector(Actor director) {
        setField(DIRECTORS, director);
    }

    public void addDirectors(List<Actor> directors) {
        setFieldWithList(DIRECTORS, directors);
    }

    // writers
    public void addWriter(Actor writer) {
        setField(WRITERS, writer);
    }

    public void addWriters(List<Actor> writers) {
        setFieldWithList(WRITERS, writers);
    }



//    == getters ==

    public List<Actor> getWriters() {
        return new ArrayList<>(writers);
    }

    public List<Actor> getDirectors() {
        return new ArrayList<>(directors);
    }

    public List<Actor> getCast() {
        return new ArrayList<>(cast);
    }

    public List<String> getGenres() {
        return new ArrayList<>(genres);
    }

    public List<String> getProduction() {
        return new ArrayList<>(production);
    }

    public String getLengthFormatted() {
        return String.format("%dh %dmin", duration /60, duration %60);
    }

    @Override
    public String getReprName() {
        return title.replaceAll(" ", "_");
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


//  == public methods ==

    public boolean isRateHigherThen(double rate) {
        if(rate > 0 && rate <= 10) {
            return this.getRate() > rate;
        } else {
            throw new IllegalArgumentException("Rate must be in range (0, 10]");
        }
    }

    public List<String> getTop3Names(List<Actor> list) {
        List<String> result = new ArrayList<>();
        if(list.size() < 4) {
            for (Actor actor : list) {
                result.add(actor.getNameAndSurname());
            }
        } else {
            for (int i = 0; i < 3; i++) {
                result.add(list.get(i).getNameAndSurname());
            }
        }
        return result;
    }

    public List<String> getDataForSummary() {
        Function<String, String> removeBrackets = string -> string.substring(1, string.length()-1);
        List<String> movieValues = new ArrayList<>();
        movieValues.add(title);
        movieValues.add(this.getLengthFormatted());
        movieValues.add(premiere.toString());
        movieValues.add(removeBrackets.apply(genres.toString()));
        movieValues.add(removeBrackets.apply(production.toString()));
        movieValues.add(removeBrackets.apply(this.getTop3Names(directors).toString()));
        movieValues.add(removeBrackets.apply(this.getTop3Names(cast).toString()));
        movieValues.add(description);
        movieValues.replaceAll(str -> {
            if(str == null) {
                return "";
            }
            return str;
        });
        return movieValues;
    }

    @Override
    public Map<String, String> getAllFieldsAsStrings() {
        Function<List<?>, String> getFromList = objects -> {
            if(objects == null || objects.size() == 0) return null;
            String tmpStr = "";
            if(objects.get(0) instanceof Actor) {
                for (Object o : objects) {
                    Actor actor = (Actor) o;
                    tmpStr = tmpStr.concat(String.valueOf(actor.getId())).concat(";");
                }
            } else if(objects.get(0) instanceof String) {
                for (Object o : objects) {
                    String field = (String) o;
                    tmpStr = tmpStr.concat(field).concat(";");
                }
            }
            tmpStr = tmpStr.substring(0, tmpStr.length()-1);
            return tmpStr;
        };

        Map<String, String> map = new LinkedHashMap<>();
        map.put(ID, String.valueOf(id));
        map.put(TITLE, title);
        map.put(TITLE_ORG, titleOrg);
        map.put(PREMIERE, premiere != null ? premiere.toString() : null);
        map.put(DURATION, String.valueOf(duration));
        map.put(RATE, String.valueOf(rate));
        map.put(RATE_COUNT, String.valueOf(rateCount));
        map.put(CAST, getFromList.apply(cast));
        map.put(DIRECTORS, getFromList.apply(directors));
        map.put(WRITERS, getFromList.apply(writers));
        map.put(GENRES, getFromList.apply(genres));
        map.put(PRODUCTION, getFromList.apply(production));
        map.put(DESCRIPTION, getDescription());
        map.put(IMAGE_PATH, imagePath != null ? imagePath.toString() : null);
        map.put(IMAGE_URL, imageUrl != null ? imageUrl.toString() : null);
        map.put(FILMWEB, filmweb.toString());
        return map;
    }

    public static LocalDate convertStrToLocalDate(String string) {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null or empty!");
        }
        return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
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

    @Override
    public boolean searchFor(String strToFind) {
        String[] strSplit = strToFind.toLowerCase().split(" ");
        for (String searchingStr : strSplit) {
            if (title.toLowerCase().contains(searchingStr)) return true;
            try {
                if(description.toLowerCase().contains(searchingStr)) return true;
            } catch (NullPointerException ignore) {}
            try {
                if(titleOrg.toLowerCase().contains(searchingStr)) return true;
            } catch (NullPointerException ignore) {}
            for(String genre : genres) {
                if(genre.toLowerCase().contains(searchingStr)) return true;
            }
            for(String prod : production) {
                if(prod.toLowerCase().contains(searchingStr)) return true;
            }
            try {
                return filmweb.toString().equals(searchingStr);
            } catch (NullPointerException ignore) {}

        }
        return false;
    }

    @Override
    public String toString() {
        String bold = "\033[1m";
        String end = "\033[0m";
        return "Movie{" +
                "id='" + bold + id + end + '\'' +
                ", title='" + bold + title + end + '\'' +
                ", premiere=" + bold + premiere + end +
                '}';
    }


    public void printPretty() {
        System.out.println("Title      : " + title);
        System.out.println("TitleOrg   : " + titleOrg);
        System.out.println("Premiere   : " + premiere);
        System.out.println("Duration   : " + getLengthFormatted());
        System.out.println("Directors  : " + directors);
        System.out.println("Writers    : " + writers);
        System.out.println("Genres     : " + genres);
        System.out.println("Production : " + production);
        System.out.println("Rate       : " + rate);
        System.out.println("RateCount  : " + rateCount);
        System.out.println("Plot       : " + description);
        System.out.println("Cast       : " + cast);
        System.out.println("WebLink    : " + filmweb);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj instanceof Movie) {
            Movie movie = (Movie) obj;
            return filmweb.equals(movie.getFilmweb());
        }
        return false;
    }

    @Override
    public int compareTo(Movie movie) {
        if(movie == null) {
            throw new IllegalArgumentException("Cannot compare to null!");
        }
//        return filmweb.toString().compareTo(movie.getFilmweb().toString());
        return title.toLowerCase().compareTo(movie.getTitle().toLowerCase());
    }

    @Override
    public void saveMe() {
        synchronized (AutoSave.NEW_OBJECTS) {
            if (!AutoSave.NEW_OBJECTS.contains(this) && !iAmFromConstructor) {
                AutoSave.NEW_OBJECTS.add(this);
                log.debug("Movie \"{}\" added to the list of new objects", this);
            }
        }
    }
}

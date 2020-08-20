package MoviesAndActors;

import FileOperations.IO;
import FileOperations.XMLOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Movie implements ContentType<Movie> {

    private static final Logger logger = LoggerFactory.getLogger(Movie.class.getName());
    private int id;
    private String title;
    private String titleOrg;
    private LocalDate premiere;
    private int length;
    private double rate;
    private int rateCount;
    private List<Actor> cast = new ArrayList<>();
    private List<Actor> directors = new ArrayList<>();
    private List<Actor> writers = new ArrayList<>();
    private List<String> genres = new ArrayList<>();
    private List<String> production = new ArrayList<>();
    private String description;
    private String imagePath;
    private String filmweb;
    private static int classMovieId;
    public static String TITLE = "title", TITLE_ORG = "titleOrg",  PREMIERE = "premiere", LENGTH = "length",
            RATE = "rate", RATE_COUNT = "rateCount", CAST = "cast", DIRECTORS = "directors", WRITERS = "writers",
            GENRES = "genres", PRODUCTION = "production", DESCRIPTION = "description";
    public static final List<String> FIELD_NAMES = new ArrayList<>(List.of(ContentType.ID, TITLE, TITLE_ORG, PREMIERE, LENGTH,
            RATE, RATE_COUNT, CAST, DIRECTORS, WRITERS, GENRES, PRODUCTION, DESCRIPTION, IMAGE_PATH, FILMWEB));

    static {
        updateClassMovieId();
    }


    public static void updateClassMovieId() {
        File movieDir = new File(XMLOperator.getSavePathMovie());
        List<String> files = IO.getFileNamesInDirectory(movieDir);
        if(files.size() == 0) {
            classMovieId = 0;
        } else {
            for (String name : files) {
                Pattern pattern = Pattern.compile("^movie(\\d+)$");
                Matcher matcher = pattern.matcher(name);
                if (matcher.find() && Integer.parseInt(matcher.group(1)) >= classMovieId) {
                    classMovieId = Integer.parseInt(matcher.group(1));
                    classMovieId++;
                }
            }
        }
    }

    public Movie(String title, LocalDate premiere) {
        updateClassMovieId();
        setFieldString("title", title);
        this.premiere = premiere;
        this.id = classMovieId;
        classMovieId++;
        saveMe();
        logger.info("New movie \"{}\" created", this.toString());
    }

    public Movie(String title, String premiere) {
        updateClassMovieId();
        setFieldString("title", title);
        setFieldString("premiere", premiere);
        this.id = classMovieId;
        classMovieId++;
        saveMe();
        logger.info("New movie \"{}\" created", this.toString());
    }

    public Movie(Map<String, List<String>> fieldMap, ContentList<Actor> allActors) {
        updateClassMovieId();
        setFieldString("title", fieldMap.get("title").get(0));
        fieldMap.remove("title");
        setFieldString("premiere", fieldMap.get("premiere").get(0));
        fieldMap.remove("premiere");
        logger.info("New movie \"{}\" created", this.toString());

        setFieldWithList("cast", allActors.convertStrIdsToObjects(fieldMap.get("cast")));
        fieldMap.remove("cast");
        setFieldWithList("directors", allActors.convertStrIdsToObjects(fieldMap.get("directors")));
        fieldMap.remove("directors");
        setFieldWithList("writers", allActors.convertStrIdsToObjects(fieldMap.get("writers")));
        fieldMap.remove("writers");

        for(String field : fieldMap.keySet()) {
            setFieldWithList(field, fieldMap.get(field));
        }
    }


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
            if(Movie.class.getDeclaredField(field).get(this) == null) {
                if (Movie.class.getDeclaredField(field).toString().contains("java.time.LocalDate")) {
                    Movie.class.getDeclaredField(field).set(this, convertStrToLocalDate(String.valueOf(value)));
                } else {
                    Movie.class.getDeclaredField(field).set(this, ContentType.checkForNullOrEmptyOrIllegalChar(String.valueOf(value), field));
                }
//                logger.debug("Field \"{}\" of \"{}\" set to \"{}\"",  field, this.toString(), Movie.class.getDeclaredField(field).get(this));
                saveMe();
            } else {
                logger.warn("Unsuccessful set of \"{}\" in movie \"{}\" - this field is already set to \"{}\"", field, this.toString(), Movie.class.getDeclaredField(field).get(this));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <E> void setFieldDigit(String field, E value) {
        if (!(value instanceof Integer) && !(value instanceof Double)) return;

        try {
            if(Movie.class.getDeclaredField(field).get(this).toString().equals("0") || Movie.class.getDeclaredField(field).get(this).toString().equals("0.0")) {
                Movie.class.getDeclaredField(field).set(this, value);
//                logger.debug("Field \"{}\" of \"{}\" set to \"{}\"",  field, this.toString(), Movie.class.getDeclaredField(field).get(this));
                saveMe();
            } else {
                logger.warn("Unsuccessful set of \"{}\" in movie \"{}\" - this field is already set to \"{}\"", field, this.toString(), Movie.class.getDeclaredField(field).get(this));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <E> void setField(String field, E value) {
        if(value == null) {
            logger.warn("Unsuccessful set of \"{}\" in \"{}\" - null as input", field, this);
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
                    logger.warn("Unsuccessful set of \"{}\" in \"{}\". \"{}\" is already on the list", field, this, value);
                    return;
                }
            } else if(value instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) Movie.class.getDeclaredField(field).get(this);
                if(!list.contains(value)) {
                    list.add((String) value);
                    Movie.class.getDeclaredField(field).set(this, list);
                } else {
                    logger.warn("Unsuccessful set of \"{}\" in \"{}\". \"{}\" is already on the list", field, this, value);
                    return;
                }
            } else {
                logger.warn("Unsuccessful set of \"{}\" in \"{}\". \"{}\" is wrong type.", field, this, value);
                return;
            }
//            logger.debug("Field \"{}\" of \"{}\" extended by \"{}\"",  field, this.toString(), value);
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

    @Override
    public String getReprName() {
        return title.replaceAll(" ", "_");
    }

    public void setLength(int length) {
        setFieldDigit(LENGTH, length);
    }

    public void setLength(String length) {
        setFieldString(LENGTH, length);
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

    public void setImagePath(String imagePath) {
        setFieldString(IMAGE_PATH, imagePath);
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

    public String getPremiereFormatted() {
        return premiere.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String getImagePath() {
        return imagePath;
    }

    public List<String> getProduction() {
        return new ArrayList<>(production);
    }

    public int getId() {
        return this.id;
    }

    public String getFilmweb() {
        return filmweb;
    }

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
        movieValues.add(this.getTitle());
        movieValues.add(this.getLengthFormatted());
        movieValues.add(this.getPremiereFormatted());
        movieValues.add(removeBrackets.apply(this.getGenres().toString()));
        movieValues.add(removeBrackets.apply(this.getProduction().toString()));
        movieValues.add(removeBrackets.apply(this.getTop3Names(this.getDirectors()).toString()));
        movieValues.add(removeBrackets.apply(this.getTop3Names(this.getCast()).toString()));
        movieValues.add(this.getDescription());
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
        map.put(PREMIERE, premiere.toString());
        map.put(LENGTH, String.valueOf(length));
        map.put(RATE, String.valueOf(rate));
        map.put(RATE_COUNT, String.valueOf(rateCount));
        map.put(CAST, getFromList.apply(cast));
        map.put(DIRECTORS, getFromList.apply(directors));
        map.put(WRITERS, getFromList.apply(writers));
        map.put(GENRES, getFromList.apply(genres));
        map.put(PRODUCTION, getFromList.apply(production));
        map.put(DESCRIPTION, getDescription());
        map.put(IMAGE_PATH, imagePath);
        map.put(FILMWEB, filmweb);
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
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", premiere=" + premiere +
                '}';
    }


    public String toCompleteString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", titleOrg='" + titleOrg + '\'' +
                ", premiere=" + premiere +
                ", length=" + length +
                ", rate=" + rate +
                ", rateCount=" + rateCount +
                ", cast=" + cast +
                ", directors=" + directors +
                ", writers=" + writers +
                ", genres=" + genres +
                ", production=" + production +
                ", description='" + description + '\'' +
                ", coverPath='" + imagePath + '\'' +
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

    @Override
    public void saveMe() {
        if(!XMLOperator.NEW_OBJECTS.contains(this)) {
            XMLOperator.NEW_OBJECTS.add(this);
            logger.debug("Movie \"{}\" added to the list of new objects", this);
        }

    }
}

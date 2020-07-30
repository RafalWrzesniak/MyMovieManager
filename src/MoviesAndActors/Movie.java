package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Movie implements ContentType<Movie> {

    private static final Logger logger = LoggerFactory.getLogger(Movie.class.getName());
    private String title;
    private String titleOrg;
    private String description;
    private String coverPath;
    private LocalDate premiere;
    private int id;
    private static int classMovieId;
    private int length;
    private int rateCount;
    private double rate;
    private final List<Actor> cast = new ArrayList<>();
    private final List<Actor> directors = new ArrayList<>();
    private final List<Actor> writers = new ArrayList<>();
    private final List<String> genres = new ArrayList<>();
    private final List<String> production = new ArrayList<>();
    public static final List<String> FIELD_NAMES = new ArrayList<>(List.of(
            "id", "title", "titleOrg", "length", "premiere", "rate", "rateCount",
            "coverPath", "description", "cast", "directors", "writers", "genres", "production"));


    static {
        classMovieId = 0;
    }

    public Movie(String title, LocalDate premiere) {
        setFieldString("title", title);
        setFieldString("premiere", premiere);
        classMovieId++;
        logger.info("New movie \"{}\" created", this.toString());
    }

    public Movie(String title, String premiere) {
        setFieldString("title", title);
        setFieldString("premiere", premiere);
        this.id = classMovieId;
        classMovieId++;
        logger.info("New movie \"{}\" created", this.toString());
    }

    public Movie(Map<String, List<String>> fieldMap, ContentList<Actor> allActors) {
        setFieldString("title", fieldMap.get("title").get(0));
        fieldMap.remove("title");
        setFieldString("premiere", fieldMap.get("premiere").get(0));
        fieldMap.remove("premiere");

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
                    Movie.class.getDeclaredField(field).set(this, checkForNullOrEmptyOrIllegalChar(String.valueOf(value), field));
                }
                logger.debug("Field \"{}\" of \"{}\" set to \"{}\"",  field, this.toString(), Movie.class.getDeclaredField(field).get(this));
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
                logger.debug("Field \"{}\" of \"{}\" set to \"{}\"",  field, this.toString(), Movie.class.getDeclaredField(field).get(this));
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
            logger.debug("Field \"{}\" of \"{}\" extended by \"{}\"",  field, this.toString(), value);
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

    public static String checkForNullOrEmptyOrIllegalChar(String stringToCheck, String argName) {
        if(stringToCheck == null) {
            throw new IllegalArgumentException(String.format("%s argument cannot be null!", argName));
        } else if(stringToCheck.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s argument cannot be empty!", argName));
        }
        for (char aChar : stringToCheck.toCharArray()) {
            if (((aChar < 20 || (aChar > 90 && aChar < 96) || (aChar > 122 && aChar < 192)) && aChar != 92)) {
                throw new IllegalArgumentException(String.format("%s argument contains illegal char: '%s' - '%d'", argName, aChar, (int) aChar));
                // && aChar != 20 && aChar != 32 && aChar != 39 && aChar != 44 && aChar != 46 && aChar != 47 && aChar != 58
            }
        }
        return stringToCheck;
    }

    public void setLength(int length) {
        setFieldDigit("length", length);
    }

    public void setLength(String length) {
        setFieldString("length", length);
    }

    public void setRate(double rate) {
        setFieldDigit("rate", rate);
    }

    public void setRateCount(int rateCount) {
        setFieldDigit("rateCount", rateCount);
    }

    public void setTitleOrg(String titleOrg) {
        setFieldString("titleOrg", titleOrg);
    }

    public void setDescription(String description) {
        setFieldString("description", description);
    }

    public void setCoverPath(String coverPath) {
        setFieldString("coverPath", coverPath);
    }


    //production
    public void addProduction(String production) {
        setField("production", production);
    }

    public void addProductions(List<String> producers) {
        setFieldWithList("production", producers);
    }


    // genres
    public void addGenre(String genre) {
        setField("genres", genre);
    }

    public void addGenres(List<String> genres) {
        setFieldWithList("genres", genres);
    }


    // actors
    public void addActor(Actor actor) {
        setField("cast", actor);
    }

    public void addActors(List<Actor> actors) {
        setFieldWithList("cast", actors);
    }

    // directors
    public void addDirector(Actor director) {
        setField("directors", director);
    }

    public void addDirectors(List<Actor> directors) {
        setFieldWithList("directors", directors);
    }

    // writers
    public void addWriter(Actor writer) {
        setField("writers", writer);
    }

    public void addWriters(List<Actor> writers) {
        setFieldWithList("writers", writers);
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

    public String getCoverPath() {
        return coverPath;
    }

    public List<String> getProduction() {
        return new ArrayList<>(production);
    }

    public int getId() {
        return this.id;
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

    public Map<String, String> getAllFields() {
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
        map.put(FIELD_NAMES.get(0), String.valueOf(id));
        map.put(FIELD_NAMES.get(1), title);
        map.put(FIELD_NAMES.get(2), titleOrg);
        map.put(FIELD_NAMES.get(3), String.valueOf(length));
        map.put(FIELD_NAMES.get(4), premiere.toString());
        map.put(FIELD_NAMES.get(5), String.valueOf(rate));
        map.put(FIELD_NAMES.get(6), String.valueOf(rateCount));
        map.put(FIELD_NAMES.get(7), coverPath);
        map.put(FIELD_NAMES.get(8), getDescription());
        map.put(FIELD_NAMES.get(9), getFromList.apply(cast));
        map.put(FIELD_NAMES.get(10), getFromList.apply(directors));
        map.put(FIELD_NAMES.get(11), getFromList.apply(writers));
        map.put(FIELD_NAMES.get(12), getFromList.apply(genres));
        map.put(FIELD_NAMES.get(13), getFromList.apply(production));
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

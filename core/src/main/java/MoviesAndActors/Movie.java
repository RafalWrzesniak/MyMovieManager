package MoviesAndActors;

import Configuration.Config;
import Configuration.Files;
import FileOperations.AutoSave;
import FileOperations.IO;
import FileOperations.StringFunctions;
import Internet.WebOperations;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Builder
@AllArgsConstructor
@EqualsAndHashCode(of = "filmweb")
public final class Movie implements ContentType, Comparable<Movie> {

//   == fields ==
    @Getter private int id;
    @Getter private String title;
    @Getter private String titleOrg;
    @Getter private String description;
    @Getter private int duration;
    @Getter private int rateCount;
    @Getter private double rate;
    @Getter private LocalDate premiere;
    @Getter private URL imageUrl;
    @Getter private URL filmweb;
    private Path imagePath;
    private final Set<Actor> cast = new LinkedHashSet<>();
    @Getter private final List<String> castIds = new ArrayList<>();
    private final Set<Actor> directors = new LinkedHashSet<>();
    @Getter private final List<String> directorIds = new ArrayList<>();
    private final Set<Actor> writers = new LinkedHashSet<>();
    @Getter private final List<String> writerIds = new ArrayList<>();
    private Set<String> genres = new HashSet<>();
    private Set<String> production = new HashSet<>();

    private static int classMovieId = -1;
    public boolean iAmFromConstructor;

//    == constants ==
    public static final String TITLE = "title", TITLE_ORG = "titleOrg",  PREMIERE = "premiere", DURATION = "duration",
            RATE = "rate", RATE_COUNT = "rateCount", CAST = "cast", DIRECTORS = "directors", WRITERS = "writers",
            GENRES = "genres", PRODUCTION = "production", DESCRIPTION = "description";
    public static final List<String> FIELD_NAMES = List.of(ContentType.ID, TITLE, TITLE_ORG, PREMIERE, DURATION,
            RATE, RATE_COUNT, CAST, DIRECTORS, WRITERS, GENRES, PRODUCTION, DESCRIPTION, IMAGE_PATH, IMAGE_URL, FILMWEB);

    public static final Comparator<Movie> COMP_ALPHABETICAL = Comparator.comparing(Movie::getTitle);
    public static final Comparator<Movie> COMP_DURATION = Comparator.comparingInt(Movie::getDuration);
    public static final Comparator<Movie> COMP_PREMIERE = Comparator.comparing(Movie::getPremiere);
    public static final Comparator<Movie> COMP_RATE = Comparator.comparingDouble(Movie::getRate);
    public static final Comparator<Movie> COMP_POPULARITY = Comparator.comparingDouble(Movie::getRateCount);


//    == constructors ==
    public Movie(Map<String, List<String>> movieMap, boolean readFromFile) {
        getNewClassMovieId();
        iAmFromConstructor = true;
        setAllNonActorFields(movieMap);
        iAmFromConstructor = false;
        log.info("New movie \"{}\" created", this);
        if(!readFromFile) saveMe();
    }


//   == private static methods ==

    public synchronized static int getNewClassMovieId() {
        File movieDir = Config.getSAVE_PATH_MOVIE().toFile();
        List<String> files = IO.getFileNamesInDirectory(movieDir);
        if(files.size() == 0 && classMovieId == -1) {
            classMovieId = 0;
        } else if(files.size() != 0){
            for (String name : files) {
                Pattern pattern = Pattern.compile("^movie(\\d+)$");
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    int readId = Integer.parseInt(matcher.group(1));
                    if(readId >= classMovieId) {
                        classMovieId = readId + 1;
                    }
                }
            }
        }
        if(classMovieId == -1) classMovieId = 0;
        return classMovieId;
    }

//    == setters ==

    private void setAllNonActorFields(Map<String, List<String>> inputMovieMap) {
        // prepare fields if not complete
        Map<String, List<String>> movieMap = new HashMap<>(inputMovieMap);
        for(String field : FIELD_NAMES) {
            if(movieMap.get(field) == null || movieMap.get(field).size() == 0) {
                List<String> nullList = new ArrayList<>();
                nullList.add(null);
                movieMap.put(field, nullList);
            }
        }
        // filmweb
        try {
            setFilmweb(new URL(movieMap.get(Movie.FILMWEB).get(0)));
        } catch (MalformedURLException ignored) {
            throw new NullPointerException("Can't create movie when filmweb is incorrect");
        }
        // id
        int id = movieMap.get(ID).get(0) == null ? -1 : Integer.parseInt(movieMap.get(ID).get(0));
        if(id == -1) {
            this.id = classMovieId;
            classMovieId++;
        } else {
            this.id = id;
        }
        // String
        setTitle(movieMap.get(Movie.TITLE).get(0));
        setTitleOrg(movieMap.get(Movie.TITLE_ORG).get(0));
        setDescription(movieMap.get(Movie.DESCRIPTION).get(0));
        // Integer
        if(movieMap.get(Movie.DURATION).get(0) != null) {
            setDuration(Integer.parseInt(movieMap.get(Movie.DURATION).get(0)));
        }
        if((movieMap.get(Movie.RATE_COUNT).get(0)) != null) {
            setRateCount(Integer.parseInt(movieMap.get(Movie.RATE_COUNT).get(0)));
        }
        // Double
        if(movieMap.get(Movie.RATE).get(0) != null) {
            setRate(Double.parseDouble(movieMap.get(Movie.RATE).get(0)));
        }
        // LocalDate
        setPremiere(StringFunctions.convertStrToLocalDate(movieMap.get(Movie.PREMIERE).get(0)));
        // Path
        if(movieMap.get(Movie.IMAGE_PATH).get(0) != null) {
            setImagePath(Paths.get(movieMap.get(Movie.IMAGE_PATH).get(0)));
        }
        // URL
        if(movieMap.get(Movie.IMAGE_URL).get(0) != null) {
            try { setImageUrl(new URL(movieMap.get(Movie.IMAGE_URL).get(0))); } catch (MalformedURLException ignored) { }
        }
        // List<String>
        addGenres(movieMap.get(Movie.GENRES));
        addProductions(movieMap.get(Movie.PRODUCTION));
        castIds.addAll(movieMap.get(Movie.CAST));
        directorIds.addAll(movieMap.get(Movie.DIRECTORS));
        writerIds.addAll(movieMap.get(Movie.WRITERS));
    }


    // String
    public void setTitle(String title) {
        try {
            this.title = ContentType.checkForNullOrEmptyOrIllegalChar(title, Movie.TITLE);
            saveMe();
        } catch (Config.ArgumentIssue ignored) { }
    }

    public void setTitleOrg(String titleOrg) {
        try {
            this.titleOrg = ContentType.checkForNullOrEmptyOrIllegalChar(titleOrg, Movie.TITLE_ORG);
            saveMe();
        } catch (Config.ArgumentIssue ignored) { }
    }

    public void setDescription(String description) {
        try {
            this.description = ContentType.checkForNullOrEmptyOrIllegalChar(description, Movie.DESCRIPTION);
            saveMe();
        } catch (Config.ArgumentIssue ignored) { }
    }


    // Integer
    public void setDuration(Integer duration) {
        if(duration <= 0) return;
        this.duration = duration;
        saveMe();
    }

    public void setRateCount(Integer rateCount) {
        if(rateCount <= 0) return;
        this.rateCount = rateCount;
        saveMe();
    }


    // Double
    public void setRate(double rate) {
        if(rate > 0 && rate <= 10) {
            this.rate = (double) (Math.round(rate * 100)) / 100;
            saveMe();
        }
    }


    // LocalDate
    public void setPremiere(LocalDate premiere) {
        this.premiere = premiere;
        saveMe();
    }


    // Path
    public void setImagePath(Path imagePath) {
        if(imagePath.getNameCount() >= 2 && !imagePath.equals(Files.NO_MOVIE_COVER)) {
            this.imagePath = imagePath.subpath(imagePath.getNameCount()-2, imagePath.getNameCount());
        } else {
            this.imagePath = imagePath;
        }
        saveMe();
    }


    // URL
    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
        saveMe();
    }

    public void setFilmweb(URL filmweb) {
        this.filmweb = filmweb;
        saveMe();
    }


    // Set<String>

    // production
    public void addProduction(String production) {
        try{
            this.production.add(ContentType.checkForNullOrEmptyOrIllegalChar(production, Movie.PRODUCTION));
            saveMe();
        } catch(Config.ArgumentIssue ignored) {}
    }
    public void addProductions(List<String> producers) {
        for(String producer : producers) {
            addProduction(producer);
        }
    }
    // genres
    public void addGenre(String genre) {
        try {
            genres.add(ContentType.checkForNullOrEmptyOrIllegalChar(genre, Movie.GENRES));
            saveMe();
        } catch(Config.ArgumentIssue ignored) {}
    }
    public void addGenres(List<String> genres) {
        for(String genre : genres) {
            addGenre(genre);
        }
    }


    // Set<Actor>

    // actors
    public void addActor(Actor actor) {
        if(actor == null) return;
        cast.add(actor);
        if(!actor.iAmFromConstructor) {
            actor.addMovieActorPlayedIn(this);
            saveMe();
        }
    }
    public void addActors(List<Actor> actors) {
        if(actors == null || actors.size() == 0) return;
        for(Actor actor : actors) {
            addActor(actor);
        }
    }

    // directors
    public void addDirector(Actor director) {
        if(director == null) return;
        directors.add(director);
        if(!director.iAmFromConstructor) {
            director.addMovieDirectedBy(this);
            saveMe();
        }
    }
    public void addDirectors(List<Actor> directors) {
        if(directors == null || directors.size() == 0) return;
        for(Actor director : directors) {
            addDirector(director);
        }
    }

    // writers
    public void addWriter(Actor writer) {
        if(writer == null) return;
        writers.add(writer);
        if(!writer.iAmFromConstructor) {
            writer.addMovieWrittenBy(this);
            saveMe();
        }
    }
    public void addWriters(List<Actor> writers) {
        if(writers == null || writers.size() == 0) return;
        for(Actor writer : writers) {
            addWriter(writer);
        }
    }



//    == getters ==

    public List<Actor> getCast() {
        return new ArrayList<>(cast);
    }

    public List<Actor> getDirectors() {
        return new ArrayList<>(directors);
    }

    public List<Actor> getWriters() {
        return new ArrayList<>(writers);
    }

    public List<String> getGenres() {
        return new ArrayList<>(genres);
    }

    public List<String> getProduction() {
        return new ArrayList<>(production);
    }

    public String getDurationFormatted() {
        return String.format("%dh %dmin", duration /60, duration %60);
    }

    public String getDurationShortFormatted() {
        String text;
        if(duration%60 < 10) {
            text = "%d:0%d";
        } else {
            text = "%d:%d";
        }
        return String.format(text, duration/60, duration%60);
    }

    @Override
    public String getReprName() {
        return title.replaceAll(" ", "_").replaceAll("[]?\\[*./:;|,\"]", "");
    }

    @Override
    public Path getImagePath() {
        if(imagePath != null && !imagePath.equals(Files.NO_MOVIE_COVER)) {
            return Config.getSAVE_PATH_MOVIE().resolve(imagePath);
        } else {
            return imagePath;
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


//  == public methods ==

    public void clearGenres() {
        genres.clear();
    }

    public void clearProduction() {
        production.clear();
    }

    public boolean isRateHigherThen(double rate) {
        if(rate > 0 && rate <= 10) {
            return this.getRate() > rate;
        } else {
            throw new IllegalArgumentException("Rate must be in range (0, 10]");
        }
    }

    public List<String> getTop3Names(Set<Actor> list) {
        Iterator<Actor> asd = list.iterator();
        List<String> result = new ArrayList<>();
        if(list.size() < 4) {
            for (Actor actor : list) {
                result.add(actor.getNameAndSurname());
            }
        } else {
            for (int i = 0; i < 3; i++) {
                result.add(asd.next().getNameAndSurname());
            }
        }
        return result;
    }

    public List<String> getDataForSummary() {
        Function<String, String> removeBrackets = string -> string.substring(1, string.length()-1);
        List<String> movieValues = new ArrayList<>();
        movieValues.add(title);
        movieValues.add(this.getDurationFormatted());
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
        Function<Set<?>, String> getFromList = setObjects -> {
            if(setObjects == null || setObjects.size() == 0) return null;
            List<?> objects = List.of(setObjects.toArray());
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
        map.put(RATE,String.valueOf(rate));
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


    @Override
    public boolean searchFor(String strToFind) {
        String searchingStr = strToFind.toLowerCase();
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
        for(Actor actor : cast) {
            if(actor.getNameAndSurname().toLowerCase().contains(searchingStr)) return true;
        }
        for(Actor director : directors) {
            if(director.getNameAndSurname().toLowerCase().contains(searchingStr)) return true;
        }
        try {
            return filmweb.toString().equals(searchingStr);
        } catch (NullPointerException ignore) {}

        return false;
    }

    @Override
    public String toString() {
        return String.format("Movie{id='%s', title='%s', premiere='%s'}", id, title, premiere);
    }

    public String printPretty() {
        String toPrint = String.format(
                "Title      : %s\n" +
                "TitleOrg   : %s\n" +
                "Premiere   : %s\n" +
                "Duration   : %s\n" +
                "Directors  : %s\n" +
                "Writers    : %s\n" +
                "Genres     : %s\n" +
                "Production : %s\n" +
                "Rate       : %s\n" +
                "RateCount  : %s\n" +
                "Plot       : %s\n" +
                "WebLink    : %s\n" +
                "Cast : %s\n"
                , title, titleOrg, premiere, getDurationFormatted(), printActors(directors), printActors(writers), genres,
                production, rate, rateCount, description, filmweb, printActors(cast));

        System.out.println(toPrint);
        return toPrint;
    }

    private String printActors(Set<Actor> listToPrint) {
        StringBuilder print = new StringBuilder();
        int actorNumber = 1;
        for (Actor actor : listToPrint) {
            print.append("\n\t").append(actorNumber).append(") ").append(actor);
            actorNumber++;
        }
        return print.toString();
    }


    @Override
    public int compareTo(Movie movie) {
        if(movie == null) {
            throw new IllegalArgumentException("Cannot compare to null!");
        }
        return title.toLowerCase().compareTo(movie.getTitle().toLowerCase());
    }

    @Override
    public void saveMe() {
        synchronized (AutoSave.NEW_OBJECTS) {
            if (!AutoSave.NEW_OBJECTS.contains(this) && !iAmFromConstructor) {
                AutoSave.NEW_OBJECTS.add(this);
                AutoSave.NEW_OBJECTS.notify();
                log.debug("Movie \"{}\" added to the list of new objects", this);
            }
        }
    }

    public static class MovieBuilder {
        protected int id;

        public MovieBuilder() {}

        protected MovieBuilder createId() {
            return id(getNewClassMovieId());
        }
    }

    public Movie withDownloadedLocalImage() {
        File movieDir = IO.createContentDirectory(this);
        Path downloadedImagePath = movieDir.toPath().resolve(this.getReprName().concat(".jpg"));
        if (WebOperations.downloadImage(this.getImageUrl(), downloadedImagePath)) {
            this.setImagePath(downloadedImagePath);
        } else {
            this.setImagePath(Files.NO_MOVIE_COVER);
        }
        return this;
    }
}

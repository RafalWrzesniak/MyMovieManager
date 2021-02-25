package Internet;

import FileOperations.IO;
import FileOperations.XMLOperator;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

@Slf4j
public final class Connection {

//    == fields ==
    private URL websiteUrl;
    @Getter private URL mainMoviePage;
    @Getter private String searchingMessage;
    private final StringBuilder failedActorsMessageBuilder = new StringBuilder("\nActors that wasn't downloaded because of low data quality:\n");

//    == constants ==
    private static final String FILMWEB = "https://www.filmweb.pl";
    private static final String LINE_WITH_MOVIE_DATA = "data-linkable=\"filmMain\"";
    private static final String LINE_WITH_MOVIE_DATA2 = "data-source=\"linksData\"";
    private static final String LINE_WITH_ACTOR_DATA = "personMainHeader";
    private static final String LINE_WITH_CAST_DATA = "data-linkable=\"filmFullCast\"";
    private static final String LINE_WITH_ACTOR_FILMOGRAPHY = "userFilmographyfalseactors";
    private static final Map<String, String> ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Actor.NAME,        "name"),
            entry(Actor.NATIONALITY, "birthPlace"),
            entry(Actor.BIRTHDAY,    "itemprop=\"birthDate\" content"),
            entry(Actor.IMAGE_URL,  "itemprop=\"image\" src")
    );
    private static final Map<String, String> MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.TITLE,      "data-title"),
            entry(Movie.TITLE_ORG,  "originalTitle"),
            entry(Movie.PREMIERE,   "releaseWorldPublicString"),
            entry(Movie.DURATION,   "duration"),
            entry(Movie.RATE,       "data-rate"),
            entry(Movie.RATE_COUNT, "dataRating-count"),
            entry(Movie.DESCRIPTION,"itemprop=\"description\""),
            entry(Movie.IMAGE_URL, "itemprop=\"image\" content")
    );
    private static final Map<String, String> MOVIE_CLASS_LIST_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.GENRES,     "gatunek"), // "genres"
            entry(Movie.PRODUCTION, "produkcja")
    );
    private static final Map<String, String> MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.CAST,     "actors"),
            entry(Movie.DIRECTORS,"director"),
            entry(Movie.WRITERS,  "screenwriter")
    );

//    == constructors ==

    public Connection(String desiredTitle) throws IOException {
        String desiredTitleEncoded = URLEncoder.encode(desiredTitle, "UTF-8");
        changeUrlTo(FILMWEB + "/search?type=film&q=" + desiredTitleEncoded);
        changeUrlTo(getMostSimilarTitleUrlFromQuery(desiredTitle));
    }

    public Connection(URL websiteUrl) {
        changeUrlTo(websiteUrl);
    }


//    == public methods ==

    public void changeMovieUrlToCastActors() throws MalformedURLException {
        this.websiteUrl = new URL(mainMoviePage.toString().concat("/cast/actors"));
    }
    public void changeMovieUrlToCastCrew() throws MalformedURLException {
        this.websiteUrl = new URL(mainMoviePage.toString().concat("/cast/crew"));
    }

    public String getFailedActorsMessage() {
        return failedActorsMessageBuilder.toString();
    }

    public File downloadWebsite() throws IOException {
        String tmpFileName =
                "tmp_"
                .concat(websiteUrl.getHost().replaceAll("(^.{3}\\.)|(\\..+)", ""))
                .concat("_")
                .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    .replaceAll("\\..*$", "")
                    .replaceAll(":", "_"))
                .concat(".html");

        ReadableByteChannel rbc = Channels.newChannel(websiteUrl.openStream());
        FileOutputStream fos = new FileOutputStream(Configuration.Files.TMP_FILES.resolve(tmpFileName).toString());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return Configuration.Files.TMP_FILES.resolve(tmpFileName).toFile();
    }

    public static boolean downloadImage(URL fromImageUrl, Path toFileName) {
        if(fromImageUrl == null || toFileName == null) return false;
        try (InputStream inputStream = fromImageUrl.openStream()) {
            Files.copy(inputStream, toFileName, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Failed to download image from \"{}\"", fromImageUrl);
            return false;
        }
        log.debug("Image downloaded from \"{}\"", fromImageUrl);
        return true;
    }

    public URL getImageUrl(boolean fromMovie) throws IOException {
        String foundLine;
        if(fromMovie) {
            foundLine = grepLineFromWebsite(LINE_WITH_MOVIE_DATA);
            if(!foundLine.contains(LINE_WITH_MOVIE_DATA2)){
                foundLine = foundLine.concat(grepLineFromWebsite(LINE_WITH_MOVIE_DATA2));
            }
            return new URL(Objects.requireNonNull(extractItemFromFilmwebLine(MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Movie.IMAGE_URL), foundLine)));
        } else {
            foundLine = grepLineFromWebsite(LINE_WITH_ACTOR_DATA);
            return new URL(Objects.requireNonNull(extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.IMAGE_URL), foundLine)));
        }
    }

    public void changeUrlTo(URL newUrl) {
        if(newUrl == null) {
            log.warn("Null as input - cannot change URL to null");
            return;
        }
        if (newUrl.toString().matches("^" + FILMWEB + "/film/[^/]+?$")) {
            this.mainMoviePage = newUrl;
        }
        this.websiteUrl = newUrl;
        if (websiteUrl.getQuery() == null) {
            log.debug("Changed Connection URL to \"{}\"", websiteUrl);
        } else {
            log.debug("Making a query to website \"{}\"", websiteUrl);
        }
    }

    public Map<String, String> grabActorDataFromFilmweb() throws IOException {
        Map<String, String> actorData = new HashMap<>();
        String foundLine = grepLineFromWebsite(LINE_WITH_ACTOR_DATA);

        String fullName = Objects.requireNonNull(
                extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NAME), foundLine))
                .replaceAll(" [iIvVxX]+$", "")
                .replaceAll(" (Jr\\.)|(Sr\\.)", "");
        if(!fullName.contains(" ")) {
            fullName = Objects.requireNonNull(
                    extractItemFromFilmwebLine("additionalName", foundLine))
                    .replaceAll(" [iIvVxX]+$", "")
                    .replaceAll(" (Jr\\.)|(Sr\\.)", "");
            if(!fullName.contains(" ")) return null;
        }
        String birthday = extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.BIRTHDAY), foundLine);
        if(birthday == null) return null;
        String[] birthPlace = replaceNullWithDash(extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NATIONALITY), foundLine)).replaceAll(" \\(.+?\\)", "").split(", ");
        String imageUrl = replaceNullWithDash(extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.IMAGE_URL), foundLine));
        String deathDay = extractDeathDateFromFilmwebLine(foundLine);

        actorData.put(Actor.NAME, fullName.substring(0, fullName.lastIndexOf(" ")));
        actorData.put(Actor.SURNAME, fullName.substring(fullName.lastIndexOf(" ") + 1));
        actorData.put(Actor.BIRTHDAY, birthday);
        actorData.put(Actor.NATIONALITY, birthPlace[birthPlace.length - 1]);
        actorData.put(Actor.FILMWEB, websiteUrl.toString());
        actorData.put(Actor.DEATH_DAY, deathDay);
        actorData.put(Actor.IMAGE_URL, imageUrl);
        log.info("Data properly grabbed from \"{}\"", websiteUrl);
        return actorData;
    }


    public Actor createActorFromFilmwebLink() throws NullPointerException, IOException {
        Map<String, String> actorMap = grabActorDataFromFilmweb();
        if(actorMap == null) return null;
        Actor actor;
        try {
            actor = new Actor(actorMap);
        } catch (Exception e) {
            log.warn("Unexpected error while creating actor from \"{}\"", actorMap);
            return null;
        }
        File actorDir = IO.createContentDirectory(actor);
        Path downloadedImagePath = Paths.get(actorDir.toString(), actor.getReprName().concat(".jpg"));
        if (Connection.downloadImage(actor.getImageUrl(), downloadedImagePath)) {
            actor.setImagePath(downloadedImagePath);
        } else {
            return null;
        }
        return actor;
    }


    public List<Actor> createActorsFromFilmwebLinks(List<String> actorUrls, ContentList<Actor> allActors, List<String> actorStringList, ContentList<Movie> allMovies) {
        List<Actor> actorList = new ArrayList<>();
        if(actorUrls == null || actorUrls.size() == 0 || allActors == null) {
             log.warn("Null or empty argument passed to createActorsFromFilmwebLinks()");
            return actorList;
        }
        for(String actorUrl : actorUrls) {
            try {
                Actor actor = allActors.getObjByUrlIfExists(new URL(actorUrl));
                if(actor == null) {
                    for(String s : actorStringList) {
                        if(s.contains(actorUrl)) {
                            String id = s.split(";")[0];
                            XMLOperator.createActorsAndAssignThemToMovies(List.of(id), allActors, allMovies);
                            actor = allActors.getObjByUrlIfExists(new URL(actorUrl));
                            break;
                        }
                    }
                }
                if(actor == null) {
                    changeUrlTo(actorUrl);
                    actor = createActorFromFilmwebLink();
                    if(actor != null) {
                        if(!allActors.add(actor)) {
                            actor = allActors.get(actor);
                        }
                    } else {
                        log.warn("Can't get data from \"{}\"", actorUrl);
                        failedActorsMessageBuilder.append(actorUrl).append("\n");
//                        throw new NullPointerException("No data found for " + actorUrl);
                    }
                } else {
                    log.debug("Actor \"{}\" already exists on \"{}\", new data won't be downloaded", actor, allActors);
                }
                actorList.add(actor);

            } catch (IOException | NullPointerException e) {
                String reason = String.format("\"%s\" in: \"%s\"", e.getMessage(), Thread.currentThread().getName());
                log.warn("Can't get data from \"{}\" because of {}", actorUrl, reason);
            }
        }
        return actorList;
    }

    public Map<String, List<String>> grabMovieDataFromFilmweb() throws IOException {
        Map<String, List<String>> movieData = new HashMap<>();
        String foundLine = grepLineFromWebsite(LINE_WITH_MOVIE_DATA);
        if(!foundLine.contains(LINE_WITH_MOVIE_DATA2)){
            foundLine = foundLine.concat(grepLineFromWebsite(LINE_WITH_MOVIE_DATA2));
        }

        for (Map.Entry<String, String> pair : MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS.entrySet()) {
            movieData.put(pair.getKey(), Collections.singletonList(extractItemFromFilmwebLine(pair.getValue(), foundLine)));
        }
        for(Map.Entry<String, String> pair : MOVIE_CLASS_LIST_FIELDS_MAP_FILMWEB_KEYS.entrySet()) {
            movieData.put(pair.getKey(), extractListOfItemsFromFilmwebLine(pair.getValue(), foundLine));
        }
        if(movieData.get(Movie.TITLE_ORG).get(0) == null) movieData.replace(Movie.TITLE_ORG, movieData.get(Movie.TITLE));
        if(movieData.get(Movie.PREMIERE).get(0) == null) {
            movieData.replace(Movie.PREMIERE, Collections.singletonList(extractItemFromFilmwebLine("releaseWorldString", foundLine)));
        }
        movieData.put(Movie.FILMWEB, Collections.singletonList(mainMoviePage.toString()));

        log.info("Data properly grabbed from \"{}\"", websiteUrl);
        return movieData;
    }

    public Movie createMovieFromFilmwebLink() throws NullPointerException, IOException {
        Map<String, List<String>> movieData;
        movieData = grabMovieDataFromFilmweb();
        Movie movie = new Movie(movieData, false);
        if(movie.getPremiere() == null) throw new NullPointerException("Couldn't find proper data of " + movie.getTitle());
        return movie;
    }

    public void addCastToMovie(Movie movie, ContentList<Actor> allActors, List<String> actorStringList, ContentList<Movie> allMovies) throws IOException {
        changeMovieUrlToCastActors();
        List<String> castUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.CAST));
        List<Actor> actors = createActorsFromFilmwebLinks(castUrls, allActors, actorStringList, allMovies);
        movie.addActors(actors);

        changeMovieUrlToCastCrew();
        List<String> directorUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.DIRECTORS));
        List<Actor> directors = createActorsFromFilmwebLinks(directorUrls, allActors, actorStringList, allMovies);
        movie.addDirectors(directors);

        changeMovieUrlToCastCrew();
        List<String> writerUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.WRITERS));
        List<Actor> writers = createActorsFromFilmwebLinks(writerUrls, allActors, actorStringList, allMovies);
        movie.addWriters(writers);
    }


    //    == private methods ==


    private URL getMostSimilarTitleUrlFromQuery(String desiredTitle) throws IOException {
        String urlToReturn = null;
        Function<String, Map<Character, Integer>> createCharCountMap = string -> {
            Map<Character, Integer> tmpMap = new HashMap<>();
            for(Character character : string.toCharArray()) {
                tmpMap.putIfAbsent(character, countChar(string, character));
            }
            return tmpMap;
        };
        Function<String, Integer> getMovieYearFromUrl = url -> {
            assert url != null;
            try {
                changeUrlTo(url);
                String lineOfCurrentTitle = grepLineFromWebsite(LINE_WITH_MOVIE_DATA).concat(LINE_WITH_MOVIE_DATA2);
                String premiereOfCurrentTitle = extractItemFromFilmwebLine(MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Movie.PREMIERE), lineOfCurrentTitle);
                if(premiereOfCurrentTitle == null) return 0;
                return LocalDate.parse(premiereOfCurrentTitle, DateTimeFormatter.ISO_DATE).getYear();
            } catch (IOException e) {
                log.warn("Couldn't extract year from url \"{}\"", url);
            }
            return 0;
        };

        Map<Character, Integer> desiredTitleCharMap = createCharCountMap.apply(desiredTitle);

        String foundLine = grepLineFromWebsite("searchMain");
        Map<String, String> foundTitleAndLinkMap = extractQueryTitleAndItsLinks(foundLine);
        if(foundTitleAndLinkMap == null || foundTitleAndLinkMap.size() == 0) return null;
        String chosenTitle = null;
        double highestCorrelation = 0;

        for(Map.Entry<String, String> titleLinkPair : foundTitleAndLinkMap.entrySet()) {
            String foundTitle = titleLinkPair.getKey();
            String foundUrl = titleLinkPair.getValue();
            double correlation, correlation1 = 0, correlation2 = 0;
            Map<Character, Integer> foundTitleCharMap = createCharCountMap.apply(foundTitle);

            for(Map.Entry<Character, Integer> pair : desiredTitleCharMap.entrySet()) {
                if(foundTitleCharMap.containsKey(pair.getKey()) && foundTitleCharMap.get(pair.getKey()).equals(desiredTitleCharMap.get(pair.getKey()))) {
                    correlation1 += desiredTitleCharMap.get(pair.getKey());
                }
            }
            correlation1 = correlation1 / desiredTitle.length();

            for(Map.Entry<Character, Integer> pair : foundTitleCharMap.entrySet()) {
                if(desiredTitleCharMap.containsKey(pair.getKey()) && foundTitleCharMap.get(pair.getKey()).equals(desiredTitleCharMap.get(pair.getKey()))) {
                    correlation2 += foundTitleCharMap.get(pair.getKey());
                }
            }
            correlation2 = correlation2 / foundTitle.length();

            correlation = (correlation1 + correlation2) / 2;
            if(correlation > highestCorrelation) {
                highestCorrelation = correlation;
                urlToReturn =  FILMWEB.concat(foundUrl);
                chosenTitle = foundTitle;
            }
            // if two movies has the same title but was released in different years
            else if(correlation == highestCorrelation && Pattern.compile(".+?\\((\\d{4})\\)$").matcher(desiredTitle).find()) {
                int desireYear = Integer.parseInt(Pattern.compile(".+?\\((\\d{4})\\)$").matcher(desiredTitle).group(1));
                int currentTitleYear = getMovieYearFromUrl.apply(urlToReturn);
                if(currentTitleYear == desireYear) {
                    continue;
                }
                int pretendedTitleYear = getMovieYearFromUrl.apply(FILMWEB.concat(foundUrl));
                if(pretendedTitleYear == desireYear) {
                    urlToReturn =  FILMWEB.concat(foundUrl);
                    chosenTitle = foundTitle;
                }
            }
//            System.out.println("Correlation of \"" + foundTitle + "\" : " + Math.round(correlation*100) + "%");
        }
        String result = String.format("\"%s\" with correlation: %d%%", chosenTitle, Math.round(highestCorrelation * 100));
        String fullResultMessage;
        if(Math.round(highestCorrelation*100) > 65) {
            fullResultMessage = String.format("For query \"%s\" there was found %s", desiredTitle, result);
            log.info(fullResultMessage);
        } else {
            fullResultMessage = String.format("For query \"%s\" there was found %s. Low correlation. Possibility of wrong movie assigning. Aborting.", desiredTitle, result);
            log.warn(fullResultMessage);
            urlToReturn = null;
        }
        this.searchingMessage = fullResultMessage + "\n";
        return urlToReturn != null ? new URL(urlToReturn) : null;
    }



    private void changeUrlTo(String newUrl) throws IOException {
        URL url = new URL(newUrl);
        changeUrlTo(url);
    }

    private String grepLineFromWebsite(String find) throws IOException {
        if(find == null) return null;
        URLConnection con;
        BufferedReader bufferedReader;
        con = websiteUrl.openConnection();
        InputStream inputStream;
        try {
            inputStream = con.getInputStream();
        } catch (SSLException e) {
            return null;
        }
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains(find)) {
                return line;
            }
        }
        log.warn("Couldn't find proper line containing \"{}\" on \"{}\"", find, websiteUrl);
        return null;
    }

    @SneakyThrows
    private String extractItemFromFilmwebLine(String itemToExtract, String line) {
        if(itemToExtract == null || line == null) {
            log.warn("Null as input - can't extract item \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            return null;
        }
        Pattern pattern = Pattern.compile(itemToExtract + "(=|\":)?+[\">]+(.+?)[\"<]");
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            return replaceAcutesHTML(matcher.group(2));
        } else {
            try {
                String newLine = grepLineFromWebsite(itemToExtract);
                if(newLine != null && !newLine.equals(line)) {
                    return extractItemFromFilmwebLine(itemToExtract, newLine);
                } else {
                    log.warn("Couldn't extract \"{}\" from \"{}\"", itemToExtract, websiteUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<String> extractListOfItemsFromFilmwebLine(String itemToExtract, String line) throws IOException {
        if(itemToExtract == null || line == null) {
            log.warn("Null as input - can't extract list of items \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            throw new IOException("Null as input - can't extract list of items \"" + itemToExtract + "\" from " + websiteUrl);
        }
        Pattern patternOfItemProp = Pattern.compile("<div class=\"filmInfo__header\">" + itemToExtract + "</div>(.+?)</div>");
        Matcher matcherOfItemProp = patternOfItemProp.matcher(line);
        List<String> listOfItems = new ArrayList<>();

        if(matcherOfItemProp.find()) {
            Pattern pattern = Pattern.compile("/ranking/film/\\D+/\\d+\">(.+?)</a>");
            Matcher matcher = pattern.matcher(matcherOfItemProp.group(1));
            while(matcher.find()) {
                listOfItems.add(replaceAcutesHTML(matcher.group(1)));
            }
        }
        if(listOfItems.size() == 0) {
            log.warn("Couldn't find any list item \"{}\" on \"{}\"", itemToExtract, websiteUrl);
        }
        return listOfItems;
    }

    @Deprecated
    @SuppressWarnings("unused")
    private List<String> extractListOfItemsFromFilmwebLineOldOne(String itemToExtract, String line) throws IOException {
        if(itemToExtract == null || line == null) {
            log.warn("Null as input - can't extract list of items \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            throw new IOException("Null as input - can't extract list of items \"" + itemToExtract + "\" from " + websiteUrl);
        }
        Pattern pattern = Pattern.compile(itemToExtract + "(=\\d+|\\w+/\\d+)\">(.+?)</a>");
        Matcher matcher = pattern.matcher(line);
        List<String> listOfItems = new ArrayList<>();
        while(matcher.find()) {
            listOfItems.add(replaceAcutesHTML(matcher.group(2)));
        }
        if(listOfItems.size() == 0) {
            try {
                String newLine = grepLineFromWebsite(itemToExtract);
                if(newLine != null && !newLine.equals(line)) {
                    return extractListOfItemsFromFilmwebLineOldOne(itemToExtract, newLine);
                } else {
                    log.warn("Couldn't find any list item \"{}\" on \"{}\"", itemToExtract, websiteUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return listOfItems;
    }

    private List<String> extractCastLinksFromFilmwebLink(String itemToExtract, String line) {
        if(itemToExtract == null || line == null) {
            log.warn("Null as input - can't extract cast links \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            return null;
        }
        List<String> listOfItems = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"profession\":\"" + itemToExtract + "\",\"person\":\\{\"name\":\".+?\",\"link\":\"(.+?)\"");
//        Pattern pattern = Pattern.compile("data-profession=\"" + itemToExtract + "\".+?<a href=\"(.+?)\">");
        Matcher matcher = pattern.matcher(line);
        int numberOfMatcher = 0;
        while(matcher.find() && numberOfMatcher < 10) {
            listOfItems.add(FILMWEB.concat(matcher.group(1)));
            numberOfMatcher++;
        }
        if(listOfItems.size() == 0) {
            log.warn("Couldn't find any cast link \"{}\" on \"{}\"", itemToExtract, websiteUrl);
        }
        return listOfItems;
    }
    
    public Set<String> extractAllMovieLinksForActor() throws IOException {
        Set<String> movieLinks = new HashSet<>();
        String line = grepLineFromWebsite(LINE_WITH_ACTOR_FILMOGRAPHY);
        if(line == null) return movieLinks;
        int indexOfActor = line.indexOf("<thead data-profession=\"actors\">") + 10;
        int indexToEndSearching = line.indexOf("<thead data-profession=", indexOfActor);
        line = line.substring(0, indexToEndSearching > 0 ? indexToEndSearching : line.length());
        Pattern pattern = Pattern.compile("\"(/film/.+?)\"");
        Matcher matcher = pattern.matcher(line);
        while(matcher.find()) {
            movieLinks.add(FILMWEB.concat(matcher.group(1)));
        }
        return movieLinks;
    }

    private String extractDeathDateFromFilmwebLine(String line) {
        if(line == null) {
            log.warn("Null as input - can't extract death date from \"{}\"", websiteUrl);
            return null;
        }
        Pattern pattern = Pattern.compile("dateToCalc=new Date\\((.+?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Map<String, String> extractQueryTitleAndItsLinks(String line) {
        if(line == null) {
            log.warn("Null as input - can't extract query data from null");
            return null;
        }
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("<data class.+?data-title=\"(.+?)\".+?href=\"(.+?)\"");
        Matcher matcher = pattern.matcher(line);
        while(matcher.find() && map.size() <= 5) {
            map.putIfAbsent(replaceAcutesHTML(matcher.group(1)), matcher.group(2));
        }
        if(map.size() == 0) {
            log.warn("Couldn't find any results of query");
        }
        return map;
    }

    private List<String> grabCastOrCrewFromFilmweb(String castType) throws IOException {
        if(castType == null || !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.CAST)) &&
                !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.DIRECTORS)) &&
                !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.WRITERS))) {
            log.warn("Wrong castType parameter, can't download cast data of \"{}\" from \"{}\"", castType, websiteUrl);
            throw new IllegalArgumentException("Wrong castType parameter");
        }
        return extractCastLinksFromFilmwebLink(castType, grepLineFromWebsite(LINE_WITH_CAST_DATA));
    }

    private static int countChar(String string, char character) {
        int hitNumber = 0;
        if(string == null || string.equals("")) return hitNumber;
        for(Character chara : string.toCharArray()) {
            if(chara == character) {
                hitNumber++;
            }
        }
        return hitNumber;
    }

    private static String replaceNullWithDash(String object) {
        return object == null ? "-" : object;
    }

    private static String replaceAcutesHTML(String str) {
        if(str == null) return null;
        str = str.replaceAll("&aacute;", "á");
        str = str.replaceAll("&eacute;", "é");
        str = str.replaceAll("&iacute;", "í");
        str = str.replaceAll("&oacute;", "ó");
        str = str.replaceAll("&uacute;", "ú");
        str = str.replaceAll("&Aacute;", "Á");
        str = str.replaceAll("&Eacute;", "É");
        str = str.replaceAll("&Iacute;", "Í");
        str = str.replaceAll("&Oacute;", "Ó");
        str = str.replaceAll("&Uacute;", "Ú");
        str = str.replaceAll("&ntilde;", "ñ");
        str = str.replaceAll("&Ntilde;", "Ñ");
        str = str.replaceAll("&egrave;", "è");
        str = str.replaceAll("&ucirc;", "û");
        str = str.replaceAll("&ocirc;", "ô");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&ouml;", "ö");
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&ndash; ", "- ");
        str = str.replaceAll("%C5%84", "ń");
        str = str.replaceAll("u0142", "ł");
        str = str.replaceAll("&oslash;", "ø");
        str = str.replaceAll("&euml;", "ë");
        str = str.replaceAll("&scaron;", "š");
        str = str.replaceAll("&yacute;", "ý");
        str = str.replaceAll("&aring;", "å");
        str = str.replaceAll("&Aring;", "Å");
        str = str.replaceAll("&agrave;", "à");
        str = str.replaceAll("Ä…","ą");
        str = str.replaceAll("Ä‡","ć");
        str = str.replaceAll("Ä™","ę");
        str = str.replaceAll("Ăł","ó");
        str = str.replaceAll("Ĺ‚","ł");
        str = str.replaceAll("Ĺ�","Ł");
        str = str.replaceAll("Ĺ„","ń");
        str = str.replaceAll("Ĺ›","ś");
        str = str.replaceAll("Ĺš","Ś");
        str = str.replaceAll("(Ĺź|ĹĽ)","ż");
        str = str.replaceAll("Ĺ»","Ż");
        str = str.replaceAll("Ĺş","ź");
        str = str.replaceAll("Ĺ","Ł");
        str = str.replaceAll("Ă“","Ó");
        str = str.replaceAll("Ăź","ü");
        str = str.replaceAll("Ă¤","ä");
        str = str.replaceAll("Ĺ‘","ö");
        str = str.replaceAll("Ĺ","Ö");
        return str;
    }
}

package Internet;

import Configuration.Config;
import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.Movie;
import lombok.Getter;
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
    @Getter
    private URL mainMoviePage;

//    == constants ==
    private static final String FILMWEB = "https://www.filmweb.pl";
    private static final String MOVIE_LINE_KEY  = "data-linkable=\"filmMain\"";
    private static final String MOVIE_LINE_KEY2 = "data-source=\"linksData\"";
    private static final String ACTOR_LINE_KEY  = "personMainHeader";
    private static final String CAST_LINE_KEY   = "data-linkable=\"filmFullCast\"";
    private static final Map<String, String> ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Actor.NAME,        "name"),
            entry(Actor.NATIONALITY, "birthPlace"),
            entry(Actor.BIRTHDAY,    "itemprop=\"birthDate\" content"),
            entry(Actor.IMAGE_URL,  "itemprop=\"image\" src")
    );
    private static final Map<String, String> MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.TITLE,      "data-title"),
            entry(Movie.TITLE_ORG,  "originalTitle"),
            entry(Movie.PREMIERE,   "releaseCountryPublicString"),
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
        changeUrlTo(FILMWEB + "/search?type=film&q=" + desiredTitleEncoded);//.replaceAll(" ", "+"));
        changeUrlTo(getMostSimilarTitleUrlFromQuery(desiredTitle));
    }

    public Connection(URL websiteUrl) throws IOException {
        changeUrlTo(websiteUrl.toString());
    }


//    == public methods ==

    public void changeMovieUrlToCastActors() throws MalformedURLException {
        this.websiteUrl = new URL(mainMoviePage.toString().concat("/cast/actors"));
    }
    public void changeMovieUrlToCastCrew() throws MalformedURLException {
        this.websiteUrl = new URL(mainMoviePage.toString().concat("/cast/crew"));
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
        FileOutputStream fos = new FileOutputStream(Config.TMP_FILES.resolve(tmpFileName).toString());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return Config.TMP_FILES.resolve(tmpFileName).toFile();
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

    public URL getImageUrl(boolean movie) throws IOException {
        String foundLine;
        if(movie) {
            foundLine = grepLineFromWebsite(MOVIE_LINE_KEY);
            if(!foundLine.contains(MOVIE_LINE_KEY2)){
                foundLine = foundLine.concat(grepLineFromWebsite(MOVIE_LINE_KEY2));
            }
            return new URL(Objects.requireNonNull(extractItemFromFilmwebLine(MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Movie.IMAGE_PATH), foundLine)));
        } else {
            foundLine = grepLineFromWebsite(ACTOR_LINE_KEY);
            return new URL(Objects.requireNonNull(extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.IMAGE_PATH), foundLine)));
        }
    }

    public void changeUrlTo(String newUrl) throws IOException {
        if(newUrl == null) {
            log.warn("Null as input - cannot change URL to null");
            throw new IOException("Null as input - cannot change URL to null");
        }
        if (newUrl.matches("^" + FILMWEB + "/film/[^/]+?$")) {
            this.mainMoviePage = new URL(newUrl);
        }
        this.websiteUrl = new URL(newUrl);
        if (websiteUrl.getQuery() == null) {
            log.debug("Changed Connection URL to \"{}\"", websiteUrl);
        } else {
            log.debug("Making a query to website \"{}\"", websiteUrl);
        }
    }


    public Actor createActorFromFilmwebLink() throws IOException, NullPointerException {
        Map<String, String> actorData = new HashMap<>();
        String foundLine = grepLineFromWebsite(ACTOR_LINE_KEY);

        String fullName = Objects.requireNonNull(
                extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NAME), foundLine))
                .replaceAll(" [iIvVxX]+", "")
                .replaceAll(" (Jr\\.)|(Sr\\.)", "");
        if(!fullName.contains(" ")) {
            fullName = Objects.requireNonNull(
                    extractItemFromFilmwebLine("additionalName", foundLine))
                    .replaceAll(" [iIvVxX]+", "")
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
        return new Actor(actorData);
    }


    public List<Actor> createActorsFromFilmwebLinks(List<String> actorUrls, ContentList<Actor> allActors) {
        List<Actor> actorList = new ArrayList<>();
        if(actorUrls == null || actorUrls.size() == 0 || allActors == null) {
             log.warn("Null or empty argument passed to createActorsFromFilmwebLinks");
            return actorList;
        }
        for(String actorUrl : actorUrls) {
            try {
                Actor actor = allActors.getObjByUrlIfExists(new URL(actorUrl));
                if(actor == null) {
                    changeUrlTo(actorUrl);
                    actor = createActorFromFilmwebLink();
                    if (allActors.add(actor)) {
                        File actorDir = IO.createContentDirectory(actor);
                        assert actor != null;
                        Path downloadedImagePath = Paths.get(actorDir.toString(), actor.getReprName().concat(".jpg"));
                        if (Connection.downloadImage(actor.getImageUrl(), downloadedImagePath)) {
                            actor.setImagePath(downloadedImagePath);
                        } else {
                            actor.setImagePath(Config.NO_IMAGE);
                        }
                    } else if (actor != null) {
                        actor = allActors.get(actor);
                    } else {
                        throw new NullPointerException("No data found for " + actorUrl);
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

    public Movie createMovieFromFilmwebLink() throws IOException, NullPointerException {
        Map<String, List<String>> movieData = new HashMap<>();
        String foundLine = grepLineFromWebsite(MOVIE_LINE_KEY);
        if(!foundLine.contains(MOVIE_LINE_KEY2)){
            foundLine = foundLine.concat(grepLineFromWebsite(MOVIE_LINE_KEY2));
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
        Movie movie = new Movie(movieData);
        if(movie.getPremiere() == null) throw new NullPointerException("Couldn't find proper data of " + movie.getTitle());
        return movie;
    }

    public void addCastToMovie(Movie movie, ContentList<Actor> allActors) throws IOException {
        changeMovieUrlToCastActors();
        List<String> castUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.CAST));
        List<Actor> actors = createActorsFromFilmwebLinks(castUrls, allActors);
        movie.addActors(actors);

        changeMovieUrlToCastCrew();
        List<String> directorUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.DIRECTORS));
        List<Actor> directors = createActorsFromFilmwebLinks(directorUrls, allActors);
        movie.addDirectors(directors);

        changeMovieUrlToCastCrew();
        List<String> writerUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.WRITERS));
        List<Actor> writers = createActorsFromFilmwebLinks(writerUrls, allActors);
        movie.addWriters(writers);
    }


    public String getMostSimilarTitleUrlFromQuery(String desiredTitle) throws IOException {
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
                String lineOfCurrentTitle = grepLineFromWebsite(MOVIE_LINE_KEY).concat(MOVIE_LINE_KEY2);
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
        String result = String.format("\"%s\" with correlation: %d%%]", chosenTitle, Math.round(highestCorrelation * 100));
        if(Math.round(highestCorrelation*100) > 50) {
            log.info("For query \"{}\" there was found {}", desiredTitle, result);
        } else {
            log.warn("For query \"{}\" there was found {}. Low correlation. Possibility of wrong movie assigning", desiredTitle, result);
        }
        return urlToReturn;
    }



    //    == private methods ==

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
            Pattern pattern = Pattern.compile("\"/ranking/film/\\w+/\\d+\">(.+?)</a>");
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
        Pattern pattern = Pattern.compile("data-profession=\"" + itemToExtract + "\".+?<a href=\"(.+?)\">");
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

    private String extractDeathDateFromFilmwebLine(String line) {
        if(line == null) {
            log.warn("Null as input - can't extract death date from \"{}\"", websiteUrl);
            return null;
        }
        Pattern pattern = Pattern.compile("dateToCalc=new Date\\((.+?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String[] split = matcher.group(1).replaceAll("2E3", "2000").split(",");
            return LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])).toString();
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
        while(matcher.find()) {
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
        return extractCastLinksFromFilmwebLink(castType, grepLineFromWebsite(CAST_LINE_KEY));
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
        return str;
    }
}

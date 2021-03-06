package MoviesAndActors;

import Configuration.Config;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ContentType  {

//    == constants ==
    String ID = "id";
    String IMAGE_PATH = "imagePath";
    String FILMWEB = "filmweb";
    String IMAGE_URL = "imageUrl";

    //    == methods ==
    Map<String, String> getAllFieldsAsStrings();
    boolean searchFor(String strToFind);
    String getReprName();
    URL getFilmweb();
    void saveMe();
    void setImagePath(Path newImagePath);
    Path getImagePath();
    int getId();

//    == default methods ==
    default LocalDate convertStrToLocalDate(String string) {
        if(string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Date argument cannot be null or empty!");
        } else if(string.equals("-")) return null;
        string = string.replaceAll("2E3", "2000").replaceAll(",", "-").replaceAll("-0-", "-1-").replaceAll("-0$", "-1");
        if(string.matches("^\\d{4}$")) {
            return LocalDate.of(Integer.parseInt(string), 1, 1);
        } else if(string.matches("^\\d{4}-\\d{1,2}$")) {
            return LocalDate.of(Integer.parseInt(string.substring(0, 4)), Integer.parseInt(string.substring(5)), 1);
        }
        try {
            return LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            String[] sepDate = string.split("-");
            return LocalDate.of(Integer.parseInt(sepDate[0]), Integer.parseInt(sepDate[1]), Integer.parseInt(sepDate[2]));
        }

    }

    static String checkForNullOrEmptyOrIllegalChar(String stringToCheck, String argName) throws Config.ArgumentIssue {
        if(stringToCheck == null) {
            throw new Config.ArgumentIssue(String.format("%s argument cannot be null!", argName));
        } else if(stringToCheck.isEmpty()) {
            throw new Config.ArgumentIssue(String.format("%s argument cannot be empty!", argName));
        }

        if(stringToCheck.matches(".*?(&.+;).*?")) {
            throw new Config.ArgumentIssue(String.format("%s argument \"%s\" contains some not formatted signs", argName, stringToCheck));
        }
        Pattern correctCharPattern;
        Pattern incorrectCharPattern;
        correctCharPattern = Pattern.compile("^(?U)[\\w\\W]+");
        incorrectCharPattern = Pattern.compile("(?U)[\\w\\W]");
        Matcher matcher = correctCharPattern.matcher(stringToCheck);
        if(!matcher.matches()) {
            matcher.usePattern(incorrectCharPattern);
            StringBuilder getAllIncorrectChars = new StringBuilder();
            while(matcher.find()) {
                if(!getAllIncorrectChars.toString().contains(stringToCheck.substring(matcher.start(), matcher.end()))) {
                    getAllIncorrectChars.append("'").append(stringToCheck, matcher.start(), matcher.end()).append("', ");
                }
            }
            getAllIncorrectChars.replace(getAllIncorrectChars.length()-2, getAllIncorrectChars.length(), "");
            throw new Config.ArgumentIssue(String.format("%s argument \"%s\" contains illegal chars: %s", argName, stringToCheck, getAllIncorrectChars.toString()));
        }
        return stringToCheck;
    }
}

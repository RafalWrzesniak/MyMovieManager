package MoviesAndActors;

import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ContentType<T> extends Comparable<T> {
    String ID = "id";
    String IMAGE_PATH = "imagePath";
    String FILMWEB = "filmweb";
    String IMAGE_URL = "imageUrl";

    boolean searchFor(String strToFind);
    int getId();
    Map<String, String> getAllFieldsAsStrings();
    String getReprName();
    void saveMe();
    URL getFilmweb();

    static String checkForNullOrEmptyOrIllegalChar(String stringToCheck, String argName) {
        if(stringToCheck == null) {
            throw new IllegalArgumentException(String.format("%s argument cannot be null!", argName));
        } else if(stringToCheck.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s argument cannot be empty!", argName));
        }

        if(stringToCheck.matches(".*?(&.+;).*?")) {
            throw new IllegalArgumentException(String.format("%s argument \"%s\" contains some not formatted signs", argName, stringToCheck));
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
                    getAllIncorrectChars.append("\'").append(stringToCheck, matcher.start(), matcher.end()).append("\', ");
                }
            }
            getAllIncorrectChars.replace(getAllIncorrectChars.length()-2, getAllIncorrectChars.length(), "");
            throw new IllegalArgumentException(String.format("%s argument \"%s\" contains illegal chars: %s", argName, stringToCheck, getAllIncorrectChars.toString()));
        }
        return stringToCheck;
    }
}
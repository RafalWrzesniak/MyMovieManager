package MoviesAndActors;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ContentType<T> extends Comparable<T> {
    String ID = "id";
    String IMAGE_PATH = "imagePath";
    String FILMWEB = "filmweb";

    boolean searchFor(String strToFind);
    int getId();
    Map<String, String> getAllFieldsAsStrings();
    String getReprName();
    void saveMe();

    static String checkForNullOrEmptyOrIllegalChar(String stringToCheck, String argName) {
        if(stringToCheck == null) {
            throw new IllegalArgumentException(String.format("%s argument cannot be null!", argName));
        } else if(stringToCheck.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s argument cannot be empty!", argName));
        }

        Pattern correctCharPattern;
        Pattern incorrectCharPattern;
        if(argName.equals(Movie.DESCRIPTION)) {
            correctCharPattern = Pattern.compile("^(?U)[\\p{Alpha}\\d\\-'., ]+");
            incorrectCharPattern = Pattern.compile("(?U)[^\\p{Alpha}\\d\\-'., ]");
        } else if(argName.equals(Movie.IMAGE_PATH) || argName.equals(FILMWEB)){
            correctCharPattern = Pattern.compile("^(?U)[\\p{Alpha}\\d\\-'.+/_:%\\\\ ]+");
            incorrectCharPattern = Pattern.compile("(?U)[^\\p{Alpha}\\d\\-'_+/.:%\\\\ ]");
        } else {
            correctCharPattern = Pattern.compile("^(?U)[\\p{Alpha}\\d\\-'.: ]+");
            incorrectCharPattern = Pattern.compile("(?U)[^\\p{Alpha}\\d\\-'.: ]");
        }

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

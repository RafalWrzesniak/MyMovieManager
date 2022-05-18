package FileOperations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringFunctions {

    @SneakyThrows
    public static JsonNode parseString(String string) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(string);
    }

    public static String slashed(String string) {
        return "/".concat(string);
    }

    public static LocalDate convertStrToLocalDate(String string) {
        if(string == null || string.isEmpty()) {
            return null;
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

    public static Map<Character, Integer> createCharCountMap(String string) {
        Map<Character, Integer> map = new HashMap<>();
        for(Character character : string.toCharArray()) {
            map.merge(character, 1, Integer::sum);
        }
        return map;
    }

    public static double calculateStringsCorrelation(String string1, String string2) {
        double correlation1 = 0, correlation2 = 0;
        Map<Character, Integer> charCountMap1 = createCharCountMap(string1);
        Map<Character, Integer> charCountMap2 = createCharCountMap(string2);
        for(Map.Entry<Character, Integer> pair : charCountMap1.entrySet()) {
            if(charCountMap2.containsKey(pair.getKey()) && charCountMap2.get(pair.getKey()).equals(charCountMap1.get(pair.getKey()))) {
                correlation1 += charCountMap1.get(pair.getKey());
            }
        }
        correlation1 = correlation1 / string1.length();

        for(Map.Entry<Character, Integer> pair : charCountMap2.entrySet()) {
            if(charCountMap1.containsKey(pair.getKey()) && charCountMap2.get(pair.getKey()).equals(charCountMap1.get(pair.getKey()))) {
                correlation2 += charCountMap2.get(pair.getKey());
            }
        }
        correlation2 = correlation2 / string2.length();

        return (correlation1 + correlation2) / 2;
    }

}
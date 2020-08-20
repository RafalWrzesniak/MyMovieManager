package Internet;

import FileOperations.IO;
import MoviesAndActors.Actor;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection {

    private final URL websiteUrl;

    public Connection(String websiteUrl) throws MalformedURLException {
        this.websiteUrl = new URL(websiteUrl);
    }

    public File downloadWebsite() throws IOException {
        String tmpFileName = "\\tmp_".concat(websiteUrl.getHost().replaceAll("(^.{3}\\.)|(\\..+)", ""))
                .concat("_").concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "").replaceAll(":", "_")).concat(".html");
        ReadableByteChannel rbc = Channels.newChannel(websiteUrl.openStream());
        FileOutputStream fos = new FileOutputStream(IO.TMP_FILES.concat(tmpFileName));
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return new File(IO.TMP_FILES.concat(tmpFileName));
    }


    public String grepLineFromWebsite(String find) throws IOException {
        URLConnection con;
        BufferedReader bufferedReader;
        con = websiteUrl.openConnection();
        InputStream inputStream = con.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains(find)) {
                return line;
            }
        }
        return null;
    }

    //<span itemprop="birthDate" content="1956-07-09"> 9 lipca 1956 </span></td></tr>
    // <td itemprop="birthPlace">Concord, Kalifornia, USA</td></tr>
    // <span itemprop="name">Tom Hanks</span>
//    <img itemprop="image" src="https://fwcdn.pl/ppo/01/24/124/449666.2.jpg" alt="Tom Hanks " class="personBigPhoto"> <i class="ico ico--copyright info-icon" id="photoInfoIcon">
    //<img itemprop="image" src="https://fwcdn.pl/ppo/41/53/4153/338048.1.jpg" alt="Rita Wilson I" class="personBigPhoto">
//            System.out.println(matcher.group(1));
//            System.out.println(matcher.group(2));
//            System.out.println(matcher.group(3));
//            System.out.println(matcher.group(4));
//            System.out.println(matcher.group(5));
//            System.out.println(matcher.group(6));

    public String extractFromLine(String itemToExtract, String line) {
        Pattern pattern = Pattern.compile(
                "<.+? itemprop=\""+itemToExtract+"\"" +
                "( content=\"(\\d{4}-\\d{2}-\\d{2})\")?+" +
                "( src=\"(.+?)\")?+( .+?=\".+?\")??>" +
                "((.+?)( [vViI]+)??" +
                "(</.+?>))?+");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find() ) {
            // content - birthday
            if(matcher.group(2) != null) {
                return matcher.group(2);
            }
            // src - image
            if(matcher.group(4) != null) {
                return matcher.group(4);
            }
            return replaceAcutesHTML(matcher.group(7));
        }
        return null;
    }

    public void downloadImage(String imageUrl, String fileName) throws IOException {
        try(InputStream inputStream = new URL(imageUrl).openStream()) {
            Files.copy(inputStream, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Map<String, String> grabActorData() throws IOException {
        String foundLine = grepLineFromWebsite("itemprop=\"birthDate\"");
        String fullName = extractFromLine("name", foundLine);
        String birthday = extractFromLine("birthDate", foundLine);
        String[] birthPlace = extractFromLine("birthPlace", foundLine).replaceAll("\\(.+?\\)", "").split(", ");
        String imageUrl = extractFromLine("image", foundLine);
        Map<String, String> actorData = new HashMap<>();
        actorData.put(Actor.NAME, fullName.substring(0, fullName.lastIndexOf(" ")));
        actorData.put(Actor.SURNAME, fullName.substring(fullName.lastIndexOf(" ")+1));
        actorData.put(Actor.BIRTHDAY, birthday);
        actorData.put(Actor.NATIONALITY, birthPlace[birthPlace.length-1]);
        actorData.put(Actor.FILMWEB, websiteUrl.toString());
        String imagePath;
        if(imageUrl != null) {
            imagePath = IO.SAVED_IMAGES.concat("\\").concat(fullName.replaceAll(" ", "_")).concat("_").concat(birthday.concat(".jpg"));
            downloadImage(imageUrl, imagePath);
        } else {
            imagePath = IO.NO_IMAGE;
        }
        actorData.put(Actor.IMAGE_PATH, imagePath);
        return actorData;
    }

    public String replaceAcutesHTML(String str) {
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
        return str;
    }
}

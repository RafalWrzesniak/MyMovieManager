package Internet;

import FileOperations.IO;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection {

    private static final String TMP_FILES = System.getProperty("user.dir").concat("\\tmp");
    static {
        File tmpFiles = new File(TMP_FILES);
        if(!tmpFiles.mkdir()) {
            IO.deleteDirectory(tmpFiles);
            tmpFiles.mkdir();
        }
    }

    public static File downloadWebsite(String website) throws IOException {
        URL websiteUrl = new URL(website);
        String tmpFileName = "\\tmp_".concat(websiteUrl.getHost().replaceAll("(^.{3}\\.)|(\\..+)", ""))
                .concat("_").concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "").replaceAll(":", "_")).concat(".html");
        ReadableByteChannel rbc = Channels.newChannel(websiteUrl.openStream());
        FileOutputStream fos = new FileOutputStream(TMP_FILES.concat(tmpFileName));
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return new File(TMP_FILES.concat(tmpFileName));
    }


    public static String grepLineFromWebsite(String website, String find) throws IOException {
        URLConnection con;
        BufferedReader bufferedReader;
        URL url = new URL(website);
        con = url.openConnection();
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

    public static String extractFromLine(String stringToExtract, String line) {
        Pattern pattern = Pattern.compile(("(<.+? itemprop=\"") + stringToExtract + "\"( content=\"(\\d{4}-\\d{2}-\\d{2})\")??>)(.+?)([vViI]+)??(</.+?>)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find() ) {
            if(matcher.group(3) != null) {
                return matcher.group(3);
            }

            String result = matcher.group(4);
            String[] results = result.split(", ");
            return results[results.length-1];
        }
        return null;
    }

}

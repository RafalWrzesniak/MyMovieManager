package MyMovieManager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskManager {

    @Getter private static final TaskManager instance = new TaskManager();
    private final Set<String> tasks = new LinkedHashSet<>();

    public void addTask(URL contentUrl) {
        if(contentUrl == null) return;
        tasks.add(contentUrl.toString());
        log.debug("Added movie source \"{}\" to task manager", contentUrl);
    }

    public void addTask(File contentFile) {
        if(contentFile == null) return;
        tasks.add(contentFile.toString());
        log.debug("Added movie source \"{}\" to task manager", contentFile);
    }

    public void addTask(List<File> fileList) {
        if(fileList == null || fileList.size() == 0) return;
        log.debug("Added {} files to task manager", fileList.size());
        fileList.forEach(file -> tasks.add(file.toString()));
        tasks.remove(null);
    }

    public void removeTask(URL contentUrl) {
        if(tasks.remove(contentUrl.toString())) {
            log.debug("\"{}\" removed from task manager", contentUrl);
        }
    }

    public void removeTask(File contentFile) {
        if(tasks.remove(contentFile.toString())) {
            log.debug("\"{}\" removed from task manager", contentFile);
        }
    }

    public String getCurrentTask() {
        if(tasks.iterator().hasNext()) {
            return tasks.iterator().next();
        }
        return null;
    }

    public int getTasksNumber() {
        return tasks.size();
    }



}

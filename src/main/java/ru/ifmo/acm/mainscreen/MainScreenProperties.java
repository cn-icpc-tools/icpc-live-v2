package ru.ifmo.acm.mainscreen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.acm.ContextListener;
import ru.ifmo.acm.backup.BackUp;
import ru.ifmo.acm.datapassing.MemesData;
import ru.ifmo.acm.events.ContestInfo;
import ru.ifmo.acm.events.EventsLoader;
import ru.ifmo.acm.events.TeamInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainScreenProperties {
    private static final Logger log = LogManager.getLogger(MainScreenProperties.class);

    public MainScreenProperties() {
        Properties properties = new Properties();
        try {
//            properties.load(getClass().getResourceAsStream("/mainscreen.properties"));
//            FileInputStream fileInputStream = new FileInputStream(
//                    new File("src/resources/mainscreen.properties"));
            properties.load(new InputStreamReader(getClass().getResourceAsStream("/mainscreen.properties"),
                    Charset.forName("UTF-8")));
        } catch (IOException e) {
            log.error("error", e);
        }

        latency = Long.parseLong(properties.getProperty("latency.time"));
        backupPersonsFilename = properties.getProperty("backup.persons");
        backupPersons = new BackUp<>(Person.class, backupPersonsFilename);
        personTimeToShow = Long.parseLong(properties.getProperty("person.time")) + latency;

        sleepTime = Integer.parseInt(properties.getProperty("sleep.time"));
        automatedShowTime = Integer.parseInt(properties.getProperty("automated.show.time"));
        automatedInfo = properties.getProperty("automated.info");
        EventsLoader loader = EventsLoader.getInstance();

        String topteamsfilename = properties.getProperty("top.teams.file");
        try {
            topteamsids = new HashSet<>();
            // topteamsids = Files.lines(Paths.get(topteamsfilename)).mapToInt(Integer::parseInt).collect(Collectors.toSet());
            Files.lines(Paths.get(topteamsfilename)).
                    forEach(topteamsids::add);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.StoppedThread loaderThread = new Utils.StoppedThread(new Utils.StoppedRunnable() {
            public void run() {
                loader.run();
            }
        });

        ContextListener.addThread(loaderThread);

        contestInfo = loader.getContestData();

        String onsiteRegex = properties.getProperty("onsite.teams", ".*");
        teamInfos = contestInfo.getStandings();
        int l = 0;
        for (int i = 0; i < teamInfos.length; i++) {
            if (teamInfos[i].getAlias().matches(onsiteRegex)) {
                teamInfos[l++] = teamInfos[i];
            }
        }
        teamInfos = Arrays.copyOf(teamInfos, l);
        Arrays.sort(teamInfos);

        loaderThread.start();

        cameraNumber = Integer.parseInt(properties.getProperty("camera.number", "0"));

        cameraURLs = new String[cameraNumber];
        cameraNames = new String[cameraNumber];
        for (int i = 0; i < cameraNumber; i++) {
            cameraURLs[i] = properties.getProperty("camera.url." + (i + 1));
            cameraNames[i] = properties.getProperty("camera.name." + (i + 1));
        }

        backupAdvertisementsFilename = properties.getProperty("backup.advertisements");
        timeAdvertisement = Long.parseLong(properties.getProperty("advertisement.time")) + latency;
        backupAdvertisements = new BackUp<>(Advertisement.class, backupAdvertisementsFilename);

        breakingNewsTimeToShow = Long.parseLong(properties.getProperty("breakingnews.time")) + latency;
        breakingNewsRunsNumber = Integer.parseInt(properties.getProperty("breakingnews.runs.number"));
        backupBreakingNewsFilename = properties.getProperty("backup.breakingnews");

        breakingNewsPatternsFilename = properties.getProperty("breakingnews.patterns.filename");

        overlayedDelay = Long.parseLong(properties.getProperty("overlayed.delay", "4000"));

        pollTimeToShow = Integer.parseInt(properties.getProperty("poll.show.time", "20000"));

        oneMemeTimeToShow = Integer.parseInt(properties.getProperty("one.meme.show.time", "5000"));
        String[] memesNames = properties.getProperty("memes", "goose").split(";");
        memes = new ArrayList<>();
        memesContent = new HashMap<>();
        for (String meme : memesNames) {
            ArrayList<String> variations = new ArrayList<>();
            for (String content : properties.getProperty("memes." + meme, "(*)>").split(";")) {
                variations.add(content);
            }
            memes.add(meme);
            memesContent.put(meme, variations);
        }
        MemesData.memesCount = new AtomicInteger[memes.size()];
        for (int i = 0; i < memes.size(); i++) {
            MemesData.memesCount[i] = new AtomicInteger();
        }
    }

    public long overlayedDelay;

    // Person
    public final long latency;
    public final BackUp<Person> backupPersons;
    public final String backupPersonsFilename;
    public final long personTimeToShow;

    // Team
    public final int sleepTime;
    public final int automatedShowTime;
    public final String automatedInfo;
    public final ContestInfo contestInfo;
    public TeamInfo[] teamInfos;
    public static HashSet<String> topteamsids;

    // Camera
    public final int cameraNumber;
    public final String[] cameraURLs;
    public final String[] cameraNames;

    // Advertisement
    public final String backupAdvertisementsFilename;
    public final long timeAdvertisement;
    public final BackUp<Advertisement> backupAdvertisements;

    // Breaking News
    public final long breakingNewsTimeToShow;
    public final int breakingNewsRunsNumber;
    public final String backupBreakingNewsFilename;

    public final String breakingNewsPatternsFilename;

    // Polls
    public final int pollTimeToShow;

    // Memes
    public final int oneMemeTimeToShow;
    public ArrayList<String> memes;
    public HashMap<String, ArrayList<String>> memesContent;
}

package com.palacesoft.server.scores;


import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

/**
 * Main service class keeping track of levels and scores.
 * Decouples write (post) from read (get) side with a FIFO queue in order to increase performance (don't block on POST or GET).
 * This means that there is some degree of eventual consistency on reads on high loads.
 * More workers could be "attached" to the FIFO queue to speed up score processing
 *
 * Note: scores have a date created field that could be used to delete old scores, otherwise RAM usage could be an issue
 *
 * @author Manuel Palacio
 */
public class DefaultScoreService implements ScoreService {

    private BlockingQueue<ScoreInfo> postQueue = new LinkedBlockingQueue<>();

    private Map<Integer, Map<Integer, NavigableSet<Integer>>> scoreData = new ConcurrentHashMap<>(8, 0.9f, 1);

    private Map<Integer, byte[]> highScores = new ConcurrentHashMap<>();

    private int highScoresMaxNumber = parseInt(getProperty("HIGH_SCORE_MAX_NO") == null ? "15" : getProperty("HIGH_SCORE_MAX_NO"));

    private ExecutorService exService = Executors.newFixedThreadPool(1);

    public DefaultScoreService() {
        exService.submit(new Worker<>(postQueue));
    }

    @Override
    public void shutDown() {
        exService.shutdownNow();
    }

    @Override
    public byte[] getHighestScoresForLevel(int level) {
        return highScores.get(level);
    }

    @Override
    public String getHighestScoresForLevelAsString(int level) {
        return new String(highScores.get(level));
    }

    @Override
    public void saveScore(ScoreInfo scoreInfo) {
        postQueue.add(scoreInfo);
    }

    private void processScore(ScoreInfo scoreInfo) {
        Map<Integer, NavigableSet<Integer>> userScores = scoreData.get(scoreInfo.getLevel());
        NavigableSet<Integer> scores;
        if (userScores == null) {
            userScores = new HashMap<>();
            scores = new TreeSet<>();
            scores.add(scoreInfo.getScore());
            userScores.put(scoreInfo.getUserId(), scores);
            scoreData.put(scoreInfo.getLevel(), userScores);
        } else {
            scores = userScores.get(scoreInfo.getUserId());
            if (scores == null) {
                scores = new TreeSet<>();
                userScores.put(scoreInfo.getUserId(), scores);
            }
            scores.add(scoreInfo.getScore());
        }
        prepareHighestScoresForLevel(scoreInfo.getLevel(), userScores);
    }

    private void prepareHighestScoresForLevel(int level, Map<Integer, NavigableSet<Integer>> userScores) {
        highScores.put(level, toCsv(findHighestScorersForLevel(userScores, level)));
    }


    private byte[] toCsv(SortedSet<ScoreInfo> scoresForLevel) {
        StringBuilder stringBuilder = new StringBuilder();
        if (scoresForLevel != null) {
            int counter = 0;
            for (ScoreInfo next : scoresForLevel) {
                if (counter >= highScoresMaxNumber) {
                    break;
                }
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(next.getUserId()).append("=").append(next.getScore());
                counter++;
            }
        }
        return stringBuilder.toString().getBytes();
    }

    private SortedSet<ScoreInfo> findHighestScorersForLevel(Map<Integer, NavigableSet<Integer>> usersAndScores, int level) {
        SortedSet<ScoreInfo> results = new TreeSet<>();
        Set<Integer> userIds = usersAndScores.keySet();
        for (Integer userId : userIds) {
            NavigableSet<Integer> userScores = usersAndScores.get(userId);
            ScoreInfo scoreInfo = new ScoreInfo(level, userId, userScores.last());
            results.add(scoreInfo);
        }
        return results;
    }

    private class Worker<T> implements Runnable {
        private final BlockingQueue<T> workQueue;

        public Worker(BlockingQueue<T> workQueue) {
            this.workQueue = workQueue;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    processScore((ScoreInfo) workQueue.take());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}

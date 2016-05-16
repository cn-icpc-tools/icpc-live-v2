package ru.ifmo.acm.events;

import java.util.Comparator;
import java.util.List;

public interface TeamInfo extends Comparable<TeamInfo> {
    int getId();

    int getRank();

    String getName();

    String getShortName();

    int getPenalty();

    int getSolvedProblemsNumber();

    long getLastAccepted();

    List<RunInfo>[] getRuns();

    String getHashTag();

    default public int compareTo(TeamInfo team) {
        return this.toString().compareTo(team.toString());
    }

    default SmallTeamInfo getSmallTeamInfo() {
        return new SmallTeamInfo(this);
    }

    default RunInfo getLastRun(int problem) {
        List<RunInfo> runs = getRuns()[problem];
        synchronized (runs) {
            if (runs.size() == 0) return null;

            for (RunInfo run : runs) {
                if ("AC".equals(run.getResult())) {
                    return run;
                }
            }

            return runs.get(runs.size() - 1);
        }
    }

    default String getShortProblemState(int problem) {
        List<RunInfo> runs = getRuns()[problem];
        synchronized (runs) {
            if (runs.size() == 0) return "";
            int total = 0;
            for (RunInfo run : runs) {
                if ("AC".equals(run.getResult())) {
                    if (total == 0)
                        return "+";
                    else
                        return "+" + total;
                }
                total++;
            }
            String finalStatus = runs.get(runs.size() - 1).getResult();
            if (finalStatus.equals("")) {
                return "?" + (total > 1 ? "" + total : total);
            } else {
                return "-" + total;
            }
        }
    }

    default boolean isReallyUnknown(int problem) {
        List<RunInfo> runs = getRuns()[problem];
        synchronized (runs) {
            for (RunInfo run : runs) {
                if ("AC".equals(run.getResult()) && !run.isReallyUnknown()) {
                    return false;
                }
                if (run.isReallyUnknown()) {
                    return true;
                }
            }
        }
        return false;
    }

    static Comparator<TeamInfo> comparator = new Comparator<TeamInfo>() {
        @Override
        public int compare(TeamInfo o1, TeamInfo o2) {
            if (o1.getSolvedProblemsNumber() != o2.getSolvedProblemsNumber()) {
                return -Integer.compare(o1.getSolvedProblemsNumber(), o2.getSolvedProblemsNumber());
            }
            if (o1.getPenalty() != o2.getPenalty()) {
                return Integer.compare(o1.getPenalty(), o2.getPenalty());
            }
            return Long.compare(o1.getLastAccepted(), o2.getLastAccepted());
        }
    };

}

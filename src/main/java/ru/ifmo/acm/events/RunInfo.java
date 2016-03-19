package ru.ifmo.acm.events;

public interface RunInfo {
    boolean isAccepted();
    boolean isJudged();
    String getResult();
    int getProblemNumber();
    long getTime();
    int getTeam();
    SmallTeamInfo getTeamInfoBefore();
}

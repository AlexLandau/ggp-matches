package net.alloyggp.matches;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

@Value.Immutable
@Value.Style()
@JsonSerialize(as = ImmutableMatchInfo.class)
@JsonDeserialize(builder = ImmutableMatchInfo.Builder.class)
public interface MatchInfo {
    String randomToken();
    ImmutableList<String> playerNamesFromHost();
    ImmutableList<ImmutableList<String>> moves();
    ImmutableList<String> states();
    long startTime();
    int playClock();
    ImmutableList<Long> stateTimes();
    ImmutableList<ImmutableList<String>> errors();
    ImmutableList<Integer> goalValues();
    String matchHostSignature();
    int startClock();
    String matchId();
    String gameMetaURL();
    String matchHostPK();
    boolean isCompleted();
}

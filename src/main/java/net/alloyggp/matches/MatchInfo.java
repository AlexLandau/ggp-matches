package net.alloyggp.matches;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@Value.Immutable
@Value.Style()
@JsonSerialize(as = ImmutableMatchInfo.class)
@JsonDeserialize(builder = MatchInfo.Builder.class)
public interface MatchInfo {
//    String randomToken();
    List<String> playerNamesFromHost(); // Not ImmutableList because there's a match record with null in here...
    ImmutableList<ImmutableList<String>> moves();
//    ImmutableList<String> states();
    long startTime();
    int playClock();
//    ImmutableList<Long> stateTimes();
//    ImmutableList<ImmutableList<String>> errors();
    ImmutableList<Integer> goalValues();
    Optional<String> matchHostSignature();
    int startClock();
    String matchId();
    String gameMetaURL();
    Optional<String> matchHostPK();
    boolean isCompleted();
//    String tournamentNameFromHost();

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Builder extends ImmutableMatchInfo.Builder {
        /*
         * This is the same as the ImmutableMatchInfo.Builder, except that it needs an additional
         * Jackson annotation that Immutables won't copy over to the builder itself.
         *
         * TODO: File Immutables issue to copy JsonIgnoreProperties annotations to the builder.
         */
    }
}

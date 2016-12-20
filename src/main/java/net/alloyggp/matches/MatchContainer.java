package net.alloyggp.matches;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style()
@JsonSerialize(as = ImmutableMatchContainer.class)
@JsonDeserialize(builder = ImmutableMatchContainer.Builder.class)
public interface MatchContainer {
    String url();
    MatchInfo data();
}

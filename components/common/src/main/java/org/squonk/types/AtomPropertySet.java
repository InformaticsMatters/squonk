package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by timbo on 29/09/2016.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AtomPropertySet implements Serializable {

    private List<Score> scores = new ArrayList<>();

    public AtomPropertySet(@JsonProperty("scores") List<Score> scores) {
        this.scores.addAll(scores);
    }

    public List<Score> getScores() {
        return scores;
    }

    @Override
    public String toString() {
        return scores.stream().map((s) -> s.toString()).collect(Collectors.joining(", "));
    }

    public static Score createScore(int atomIndex, String atomSymbol, Float score, Integer rank) {
        return new Score(atomIndex, atomSymbol, score, rank);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Score implements Serializable {

        /**
         * zero based atom index in the molecule
         */
        private final int atomIndex;
        /**
         * The atom symbol e.g. C
         */
        private final String atomSymbol;
        /**
         * The score
         */
        private final Float score;
        /**
         * Optional rank for the property, 1 being the highest. There can be ties
         */
        private final Integer rank;

        public Score(
                @JsonProperty("atomIndex") int atomIndex,
                @JsonProperty("atomSymbol") String atomSymbol,
                @JsonProperty("score") Float score,
                @JsonProperty("rank") Integer rank) {
            this.atomIndex = atomIndex;
            this.atomSymbol = atomSymbol;
            this.score = score;
            this.rank = rank;
        }

        public int getAtomIndex() {
            return atomIndex;
        }

        public String getAtomSymbol() {
            return atomSymbol;
        }

        public Float getScore() {
            return score;
        }

        public Integer getRank() {
            return rank;
        }

        @Override
        public String toString() {
            return (rank == null ? "" : rank + " ") + atomSymbol + "." + atomIndex + "=" + score;
        }
    }
}

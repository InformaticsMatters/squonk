/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.DepictionParameters;
import org.squonk.util.Colors;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 29/09/2016.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AtomPropertySet implements Serializable, Comparable<AtomPropertySet>, MoleculeObjectHighlightable {

    private static final Logger LOG = Logger.getLogger(AtomPropertySet.class.getName());

    private List<Score> scores = new ArrayList<>();

    public AtomPropertySet(@JsonProperty("scores") List<Score> scores) {
        this.scores.addAll(scores);
    }

    public List<Score> getScores() {
        return scores;
    }

    @Override
    public String toString() {
        return scores.stream().map((s) -> s.toString()).collect(Collectors.joining("\n"));
    }

    public static Score createScore(int atomIndex, String atomSymbol, Float score, Integer rank) {
        return new Score(atomIndex, atomSymbol, score, rank);
    }

    public void highlight(DepictionParameters dp,
                          Color startColor, Color endColor,
                          float startValue, float endValue,
                          DepictionParameters.HighlightMode mode, boolean highlightBonds) {
        for (AtomPropertySet.Score score : getScores()) {
            int atomIndex = score.getAtomIndex();
            Float value = score.getScore();
            if (value != null) {
                float f = (value - startValue) / (endValue - startValue);
                Color color = Colors.interpolateRGBLinear(startColor, endColor, f);
                LOG.finer("Highlighting atom " + atomIndex + " as " + Colors.rgbaColorToHex(color));
                dp.addAtomHighlight(new int[]{atomIndex}, color, mode, highlightBonds);
            }
        }
    }

    @Override
    public int compareTo(AtomPropertySet o) {
        List<Float> mine = scores.stream().map((s) -> s.getScore()).sorted().collect(Collectors.toList());
        List<Float> others = o.getScores().stream().map((s) -> s.getScore()).sorted().collect(Collectors.toList());
        for (int i=0; i < mine.size(); i++) {
            if (others.size() > i) {
                int c = mine.get(i).compareTo(others.get(i));
                if (c != 0) {
                    return c;
                }
            } else {
                return 1;
            }
        }
        return 0;
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

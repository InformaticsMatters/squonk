/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.util;

import java.util.logging.Logger;

/**
 * Hump functions for transforming numerical values into standardized values for scoring functions.
 * Use in things like the MPO score.
 * The approach is generalised but typically fall into 2 categories:
 * <ol>
 *     <li>Ramp function starting with one value (e.g. 0) and transitioning to another value (e.g. 1)
 *     linearly over a specified range (e.g. 3 to 5). With those parameters any input less than or equal to
 *     3 scores 0, the input 4 scores 0.5 and any input greater than 5 scores 1.</li>
 *     <li>Hump function starting with one value, having a second value with its corresponding ramp (as in
 *     the ramp function) and then a final value with its corresponding ramp. e.g. start at 0, go up to 1 over a range
 *     of 3 to 4 and then back down to 0 over the range to 5 to 6.</li>
 * </ol>
 * The hump function is generalised to handle any number of humps. There are specific factory methods for ramp, hump1
 * and hump2 plus a generic factory method for an arbitrary number of humps.
 *
 * In all cases the threshold values MUST be specified in ascending order.
 *
 *  Created by timbo on 5/3/18.
 */
public class MpoFunctions {

    private static final Logger LOG = Logger.getLogger(MpoFunctions.class.getName());

    /** Factory method for the basic ramp function.
     * This could also be thought of as a createHump0Function() factory method.
     *
     * @param initialScore The score for values that are below the lower threshold.
     * @param finalScore The score for values that are above the upper threshold.
     * @param beginValue The value at which the transition starts.
     * @param endValue  The value at which the transition ends. Must be greater than beginValue.
     * @return The transformed value.
     */
    public static NumberTransform createRampFunction(double initialScore, double finalScore, double beginValue, double endValue) {
        double[] first = {finalScore, beginValue, endValue};
        return new HumpFunction(initialScore, new double[][] {first});
    }

    /** Generalised factory method for creating hump functions.
     *
     * @param initialScore The score for values that are below the lower threshold.
     * @param params An array of arrays of double values. Each sub-array is of size 3 and has the hump score as index 0,
     *               the starting value as index 1 and the end value as index 2.
     * @return The transformed value
     */
    public static NumberTransform createHumpFunction(double initialScore, double[][] params) {
        return new HumpFunction(initialScore, params);
    }

    /** Factory method for a simple hump function with a starting score a "plateau" score and a final score along with
     * thresholds for regions over which these scores transition.
     *
     * @param score0 The starting (lowest values) score.
     * @param score1 The "plateau" score.
     * @param score2 The ending (highest values) score.
     * @param beginValue1 The starting value for the first transition.
     * @param endValue1 The ending value for the first transition. Must be greater than beginValue1.
     * @param beginValue2 The starting value for the second transition. Must be greater than endValue1.
     * @param endValue2 The starting value for the second transition. Must be greater than beginValue2.
     * @return The transformed value
     */
    public static NumberTransform createHump1Function(double score0, double score1, double score2,
                                                      double beginValue1, double endValue1,
                                                      double beginValue2, double endValue2) {
        double[] first = {score1, beginValue1, endValue1};
        double[] second = {score2, beginValue2, endValue2};
        return new HumpFunction(score0, new double[][] {first, second});
    }

    /** Factory method for a double hump function with a starting score a first "plateau" score, a second "plateau" score
     * and a final score along with thresholds for regions over which these scores transition.
     *
     * @param score0 The starting (lowest values) score.
     * @param score1 The first "plateau" score.
     * @param score2 The second "plateau" score.
     * @param score3 The ending (highest values) score.
     * @param beginValue1 The starting value for the first transition.
     * @param endValue1 The ending value for the first transition. Must be greater than beginValue1.
     * @param beginValue2 The starting value for the second transition.  Must be greater than endValue1.
     * @param endValue2 The ending value for the second transition.  Must be greater than beginValue2.
     * @param beginValue3 The starting value for the third transition.  Must be greater than endValue2.
     * @param endValue3 The ending value for the third transition.  Must be greater than beginValue3.
     * @return The transformed value
     */
    public static NumberTransform createHump2Function(double score0, double score1, double score2, double score3,
                                                      double beginValue1, double endValue1,
                                                      double beginValue2, double endValue2,
                                                      double beginValue3, double endValue3) {
        double[] first = {score1, beginValue1, endValue1};
        double[] second = {score2, beginValue2, endValue2};
        double[] third = {score3, beginValue3, endValue3};
        return new HumpFunction(score0, new double[][] {first, second, third});
    }

    private static class HumpFunction implements NumberTransform {

        final double beginScore;
        final double[][] params;

        /**
         *
         * @param beginScore The score for values that are lower than params[0][1]
         * @param params Array of 3-element arrays. Each sub-array has the hump score as index 0, the starting
         *               value as index 1 and the end value as index 2
         */
        HumpFunction(double beginScore, double[][] params) {
            assert params != null;
            assert params.length > 0;
            for (int i=0; i<params.length; i++) {
                double[] p = params[i];
                assert p.length == 3;
                assert p[1] < p[2];
                if (i > 0) {
                    assert p[1] >= params[i-1][2];
                }
            }

            this.beginScore = beginScore;
            this.params = params;
        }

        @Override
        public Double transform(Double value) {
            if (value == null) {
                return null;
            }
            double score = beginScore;
            for (int i=0; i<params.length; i++) {
                //LOG.info("Iter " + i);
                double[] p = params[i];
                if (value <= p[1]) {
                    return score;
                } else if (value <= p[2]) {
                    double adjustment = (p[0] - score) * (value - p[1]) / (p[2] - p[1]);
                    return score + adjustment;
                }
                score = p[0];
            }

            return score;
        }
    }


}

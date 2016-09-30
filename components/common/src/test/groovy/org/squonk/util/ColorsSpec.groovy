package org.squonk.util

import org.squonk.io.DepictionParameters
import org.squonk.types.AtomPropertySet
import spock.lang.Specification

/**
 * Created by timbo on 30/09/2016.
 */
class ColorsSpec extends Specification {

    void "generate highlights"() {

        List<AtomPropertySet.Score> scores = []
        scores.add(AtomPropertySet.createScore(2, 'C', 49.85, 1))
        scores.add(AtomPropertySet.createScore(0, 'C', 53.65, 2))
        scores.add(AtomPropertySet.createScore(9, 'C', 67.01, 3))
        AtomPropertySet aps = new AtomPropertySet(scores)

        DepictionParameters p = new DepictionParameters()

        when:
        Colors.generateHighlights(aps, p, Colors.BROWN, Colors.STEELBLUE, 40, 100, DepictionParameters.HighlightMode.region, false)

        then:
        p.getHighlights().size() == 3

    }
}

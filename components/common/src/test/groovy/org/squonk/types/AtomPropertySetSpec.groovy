package org.squonk.types

import org.squonk.io.DepictionParameters
import org.squonk.types.io.JsonHandler
import org.squonk.util.Colors
import spock.lang.Specification

/**
 * Created by timbo on 29/09/2016.
 */
class AtomPropertySetSpec extends Specification {

    void "to/from json"() {

        def scores = []
        scores.add(AtomPropertySet.createScore(0, 'C', 1.1f, 1))
        scores.add(AtomPropertySet.createScore(2, 'N', 2.2f, 2))
        def aps1 = new AtomPropertySet(scores)

        when:
        String json = JsonHandler.getInstance().objectToJson(aps1)
        AtomPropertySet aps2 = JsonHandler.getInstance().objectFromJson(json, AtomPropertySet.class)

        then:
        json != null
        aps2 != null
        aps2.getScores().size() == 2
        aps2.getScores()[0].atomIndex == 0
        aps2.getScores()[0].atomSymbol == 'C'
        aps2.getScores()[0].score == 1.1f
        aps2.getScores()[0].rank == 1

    }

    void "generate highlights"() {

        List<AtomPropertySet.Score> scores = []
        scores.add(AtomPropertySet.createScore(2, 'C', 49.85, 1))
        scores.add(AtomPropertySet.createScore(0, 'C', 53.65, 2))
        scores.add(AtomPropertySet.createScore(9, 'C', 67.01, 3))
        AtomPropertySet aps = new AtomPropertySet(scores)

        DepictionParameters p = new DepictionParameters()

        when:
        aps.highlight(p, Colors.BROWN, Colors.STEELBLUE, 40, 100, DepictionParameters.HighlightMode.region, false)

        then:
        p.getHighlights().size() == 3

    }

    void "compare descending first"() {

        AtomPropertySet aps1 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(0, 'C', 53.65, 2),
                AtomPropertySet.createScore(9, 'C', 67.01, 3)
        ])
        AtomPropertySet aps2 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 39.85, 1),
                AtomPropertySet.createScore(3, 'C', 53.65, 2),
                AtomPropertySet.createScore(4, 'C', 67.01, 3)
        ])

        when:
        int c = aps1.compareTo(aps2)

        then:
        c == 1
    }

    void "compare ascending first"() {

        AtomPropertySet aps1 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(0, 'C', 53.65, 2),
                AtomPropertySet.createScore(9, 'C', 67.01, 3)
        ])
        AtomPropertySet aps2 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 59.85, 1),
                AtomPropertySet.createScore(3, 'C', 53.65, 2),
                AtomPropertySet.createScore(4, 'C', 67.01, 3)
        ])

        when:
        int c = aps1.compareTo(aps2)

        then:
        c == -1
    }

    void "compare descending second"() {

        AtomPropertySet aps1 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(0, 'C', 43.65, 2),
                AtomPropertySet.createScore(9, 'C', 67.01, 3)
        ])
        AtomPropertySet aps2 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(3, 'C', 33.65, 2),
                AtomPropertySet.createScore(4, 'C', 67.01, 3)
        ])

        when:
        int c = aps1.compareTo(aps2)

        then:
        c == 1
    }

    void "compare ascending second"() {

        AtomPropertySet aps1 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(0, 'C', 13.65, 2),
                AtomPropertySet.createScore(9, 'C', 67.01, 3)
        ])
        AtomPropertySet aps2 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(3, 'C', 33.65, 2),
                AtomPropertySet.createScore(4, 'C', 67.01, 3)
        ])

        when:
        int c = aps1.compareTo(aps2)

        then:
        c == -1
    }

    void "compare unbalanced"() {

        AtomPropertySet aps1 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(0, 'C', 13.65, 2),
                AtomPropertySet.createScore(9, 'C', 67.01, 3)
        ])
        AtomPropertySet aps2 = new AtomPropertySet([
                AtomPropertySet.createScore(2, 'C', 49.85, 1),
                AtomPropertySet.createScore(3, 'C', 13.65, 2),
        ])

        when:
        int c = aps1.compareTo(aps2)

        then:
        c == 1
    }


}

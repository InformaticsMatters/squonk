package org.squonk.types

import org.squonk.types.io.JsonHandler
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

}

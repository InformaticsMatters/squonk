package org.squonk.camel.chemaxon.processor.enumeration;

import chemaxon.formats.MolImporter;
import chemaxon.reaction.Reactor;
import chemaxon.struc.Molecule;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.squonk.chemaxon.enumeration.ReactionLibrary;
import org.squonk.chemaxon.enumeration.ReactorExecutor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.util.CamelRouteStatsRecorder;
import org.squonk.util.StatsRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class ReactorProcessor implements Processor {

    private static Logger LOG = Logger.getLogger(ReactorProcessor.class.getName());

    private final String statsRouteUri;

    public static final String OPTION_REACTOR_REACTION = "reaction";
    public static final String OPTION_IGNORE_REACTIVITY = "ignoreReactivityRules";
    public static final String OPTION_IGNORE_SELECTIVITY = "ignoreSelectivityRules";
    public static final String OPTION_IGNORE_TOLERANCE = "ignoreToleranceRules";
    public static final String OPTION_REACTOR_OUTPUT = "reactorOutput";

    private final String rxnLibZipFile;
    private ReactionLibrary rxnlib;

    public ReactorProcessor(String rxnLibZipFile, String statsRouteUri) {
        this.rxnLibZipFile = rxnLibZipFile;
        this.statsRouteUri = statsRouteUri;

        File f = new File(rxnLibZipFile);
        if (f.exists()) {
            try {
                rxnlib = new ReactionLibrary(f);
                LOG.info("Using reaction library from " + rxnLibZipFile);
            } catch (IOException ioe) {
                LOG.warning("Could not load reaction library from " + rxnLibZipFile);
            }
        } else {
            LOG.warning("Reaction library not found at " + rxnLibZipFile);
        }
    }

    @Override
    public void process(Exchange exch) throws Exception {

        if (rxnlib == null) {
            throw new FileNotFoundException("Reaction library could not be found at " + rxnLibZipFile);
        }

        // 1. the reaction
        String reactionName = exch.getIn().getHeader(OPTION_REACTOR_REACTION, String.class);
        if (reactionName == null) {
            throw new IllegalStateException("No reaction name specified. Should be present as header named " + OPTION_REACTOR_REACTION);
        }
        String reactionMrv = readReaction(reactionName);
        if (reactionMrv == null) {
            throw new IllegalStateException("Reaction " + reactionName + " could not be found");
        }
        Molecule rxn = MolImporter.importMol(reactionMrv);

        // 2. the reactants
        Dataset<MoleculeObject> dataset = exch.getIn().getBody(Dataset.class);

        // 3. other options
        Boolean ignoreReactivity = exch.getIn().getHeader(OPTION_IGNORE_REACTIVITY, Boolean.class);
        Boolean ignoreSelectivity = exch.getIn().getHeader(OPTION_IGNORE_SELECTIVITY, Boolean.class);
        Boolean ignoreTolerance = exch.getIn().getHeader(OPTION_IGNORE_TOLERANCE, Boolean.class);

        int ignoreRules = (ignoreReactivity == null || !ignoreReactivity ? 0 : Reactor.IGNORE_REACTIVITY)
                | (ignoreSelectivity == null || !ignoreSelectivity  ? 0 : Reactor.IGNORE_SELECTIVITY)
                | (ignoreTolerance == null || !ignoreTolerance  ? 0 : Reactor.IGNORE_TOLERANCE);

        LOG.info(String.format("Rules: %s [%s %s %s]", ignoreRules, ignoreReactivity, ignoreSelectivity, ignoreTolerance));

        String header = exch.getIn().getHeader(OPTION_REACTOR_OUTPUT, String.class);
        ReactorExecutor.Output output =  (header == null ? ReactorExecutor.Output.Product1 : ReactorExecutor.Output.valueOf(header));

        // stats generation
        String jobId = exch.getIn().getHeader(StatsRecorder.HEADER_SQUONK_JOB_ID, String.class);
        StatsRecorder statsRecorder = null;
        if (statsRouteUri != null) {
            ProducerTemplate pt = exch.getContext().createProducerTemplate();
            pt.setDefaultEndpointUri(statsRouteUri);
            statsRecorder = new CamelRouteStatsRecorder(jobId, pt);
        }

        // perform the enumeration
        ReactorExecutor exec = new ReactorExecutor(rxn, statsRecorder);
        Stream<MoleculeObject> results = exec.enumerateMoleculeObjects(output, ignoreRules, dataset.getStream());
        results = results.onClose(() -> {

        });

        // TODO - handle stats

        exch.getIn().setBody(new MoleculeObjectDataset(results));
    }

    private String readReaction(String name) throws IOException {
//        String resource = "/reactions/" + name + ".mrv";
//        LOG.info("Reading reaction from " + resource);
//        InputStream is = this.getClass().getResourceAsStream(resource);
//        return is == null ? null : IOUtils.convertStreamToString(is);

        return rxnlib.getReaction(name);
    }



}

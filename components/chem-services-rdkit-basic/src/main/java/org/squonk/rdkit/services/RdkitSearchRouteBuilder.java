package org.squonk.rdkit.services;


import org.apache.camel.builder.RouteBuilder;


/**
 * Chemical search based on RDKit cartridge
 *
 * @author timbo
 */
public class RdkitSearchRouteBuilder extends RouteBuilder {


    private  String routeName = "foo";

//    public RdkitSearchRouteBuilder(RDKitTableSearch searcher, String routeName) {
//        this.searcher = searcher;
//        this.routeName = routeName;
//    }

    @Override
    public void configure() throws Exception {

        from(routeName)
                .log("$routeName starting")
                //.threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                //.unmarshal().json(JsonLibrary.Jackson, StructureSearch.class)
//                .process((Exchange exch) -> {
//                    StructureSearch search = exch.getIn().getBody(StructureSearch.class);
//                    List<MoleculeObject> results = searcher.search(search);
//                    exch.getIn().setBody(new Dataset(MoleculeObject.class, results));
//                })
                .log("$routeName finished");

    }

//    public static void main(String[] args) throws Exception {
//
//        Map<String, String> extraColumnDefs = new HashMap<>();
//        extraColumnDefs.put("version_id", "INTEGER");
//        extraColumnDefs.put("parent_id", "INTEGER");
//
//        PGSimpleDataSource ds = new PGSimpleDataSource();
//        //String server = System.getenv("RDKIT_CART_SERVER");
//        //ds.setServerName(server == null ? "localhost" : server);
//        ds.setServerName("192.168.99.100");
//        ds.setPortNumber(5432);
//        ds.setDatabaseName("rdkit");
//        ds.setUser("docker");
//        ds.setPassword("docker");
//
//
//        RDKitTableSearch emolsBBSearcher = new RDKitTableSearch(ds, "public", "emolecules_order_bb", RDKitTable.MolSourceType.CTAB, extraColumnDefs);
//
//
//        CamelContext context = new DefaultCamelContext();
//        String route = "direct:rdkitEmoleculesBBSearch";
//        context.addRoutes(new RdkitSearchRouteBuilder(emolsBBSearcher, route));
//        context.submit();
//        try {
//            ProducerTemplate pt = context.createProducerTemplate();
//            StructureSearch search = new SubstructureSearch("[#8]-[#6](=O)-[#6]-1=[#6]-[#6]=[#6]-[#6]=[#6]-1-[#8]", false, 100);
//            Dataset results = pt.requestBody(route, search, Dataset.class);
//            System.out.println("Results: " + results.getItems().size());
//
//            search = new SimilaritySearch("OC(=O)C1=CC=CC=C1O", 0.6, RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.TANIMOTO, 100);
//            results = pt.requestBody(route, search, Dataset.class);
//            System.out.println("Results: " + results.getItems().size());
//
//            System.out.println("Done");
//        } finally {
//            context.stop();
//        }
//    }
}

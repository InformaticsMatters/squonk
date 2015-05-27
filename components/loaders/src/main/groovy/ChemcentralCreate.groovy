import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import groovy.sql.Sql
import java.sql.*
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource

/**
 * Drops and/or creates the chemcentral tables. 
 * Define details in database.properties and chemcentral.properties
 * @author timbo
 */
class ChemcentralCreate  {
    
    ConfigObject database
    private String structureTable
    DataSource dataSource

   
    static void main(String[] args) {
        println "Running with $args"
        
        def instance = new ChemcentralCreate()
        //instance.run(args)
        instance.run(['dropTables', 'createTables'] as String[])
        //instance.run(['dropTables'] as String[])
    }
    
    ChemcentralCreate() {
        database = Utils.createConfig('database.properties')
        this.structureTable = database.chemcentral.schema + '.structures'
        dataSource = Utils.createDataSource(database, database.chemcentral.username, database.chemcentral.password)
    }
    
    void run(String[] args) {
        if (!database.allowRecreate) {
            println "Will not modify. Please set allowRecreate property in chemcentral.properties to true to permit this"
        } else {
            args.each { action ->
                if (action == 'createTables') {
                    createTables(database.chemcentral)
                } else if (action == 'dropTables') {
                    dropTables(database.chemcentral)
                } else {
                    println "Unrecognised action: $action"
                }
            }
        }
    }
    
    ConnectionHandler createConnectionHandler() {
        new ConnectionHandler(dataSource.getConnection(), database.chemcentral.schema + '.jchemproperties')
    }

    void dropTables(def props) {
        ConnectionHandler conh = createConnectionHandler()
        Sql db = new Sql(conh.getConnection())
        Utils.with {
            executeMayFail(db, "drop table ${props.schema}.structure_props", 'DROP TABLE ' + props.schema + '.structure_props')
            executeMayFail(db, "drop table ${props.schema}.instances", 'DROP TABLE ' + props.schema + '.instances')
            executeMayFail(db, "drop table ${props.schema}.property_definitions", 'DROP TABLE ' + props.schema + '.property_definitions')
            executeMayFail(db, "drop table ${props.schema}.structure_aliases", 'DROP TABLE ' + props.schema + '.structure_aliases')
            executeMayFail(db, "drop table ${props.schema}.sources", 'DROP TABLE ' + props.schema + '.sources')
            executeMayFail(db, "drop table ${props.schema}.categories", 'DROP TABLE ' + props.schema + '.categories')
        }
        if (UpdateHandler.isStructureTable(conh, structureTable)) {
            println "dropping structure table $structureTable"
            UpdateHandler.dropStructureTable(conh, structureTable)
        }
    }
    
    void createTables(def props) { 
        ConnectionHandler conh = createConnectionHandler()
        Sql db = new Sql(conh.getConnection())
 
        if (!DatabaseProperties.propertyTableExists(conh)) {
            println "creating property table"
            DatabaseProperties.createPropertyTable(conh)    
        }
            
        if (!UpdateHandler.isStructureTable(conh, structureTable)) {
            println "creating structure table $structureTable"
            StructureTableOptions opts = new StructureTableOptions(structureTable, TableTypeConstants.TABLE_TYPE_MOLECULES)
            opts.extraColumnDefinitions = 'parent_id INTEGER, frag_count SMALLINT'
            //opts.structureColumnType = 
            //,psa FLOAT, logp FLOAT, hba SMALLINT, hbd SMALLINT, rot_bond_count SMALLINT

            opts.chemTermColsConfig = [
                frag_count: 'fragmentCount()'
                //                            psa: 'psa()', 
                //                            logp: 'logp()', 
                //                            hba: 'acceptorCount()', 
                //                            hbd: 'donorCount()',
                //                            rot_bond_count: 'rotatableBondCount()'
            ]
            opts.standardizerConfig = new File(props.standardizer).text
            UpdateHandler.createStructureTable(conh, opts)
            Utils.with {    
                execute(db, "create table ${props.schema}.categories",  """\
                    |CREATE TABLE ${props.schema}.categories (
                    |  id SERIAL PRIMARY KEY,
                    |  category_name VARCHAR(16),
                    |  CONSTRAINT uq_category_name UNIQUE (category_name)
                    |)""".stripMargin())
                
                execute(db, "create table ${props.schema}.sources",  """\
                    |CREATE TABLE ${props.schema}.sources (
                    |  id SERIAL PRIMARY KEY,
                    |  category_id integer NOT NULL,
                    |  source_name TEXT,
                    |  source_version TEXT,
                    |  source_description TEXT,
                    |  source_uri TEXT,
                    |  type CHAR(1) NOT NULL,
                    |  owner VARCHAR(50) NOT NULL,
                    |  maintainer VARCHAR(50) NOT NULL,
                    |  active BOOLEAN DEFAULT TRUE,
                    |  CONSTRAINT fk_sources2categories FOREIGN KEY (category_id) REFERENCES ${props.schema}.categories(id),
                    |  CONSTRAINT uq_source_name_version UNIQUE (source_name, source_version)
                    |)""".stripMargin())
                
                execute(db, "create table ${props.schema}.structure_aliases", """\
                    |CREATE TABLE ${props.schema}.structure_aliases (
                    |  id SERIAL PRIMARY KEY,
                    |  structure_id INTEGER NOT NULL,
                    |  source_id INTEGER NOT NULL,
                    |  alias_value VARCHAR(32) NOT NULL,
                    |  CONSTRAINT fk_sa2sources FOREIGN KEY (source_id) REFERENCES ${props.schema}.sources(id) ON DELETE CASCADE,
                    |  CONSTRAINT fk_sa2structures FOREIGN KEY (structure_id) REFERENCES ${props.schema}.structures(cd_id) ON DELETE CASCADE
                    |)""".stripMargin())
                execute(db, 'add index idx_sa_structure_id',  'CREATE INDEX idx_sa_structure_id ON ' + props.schema + '.structure_aliases(structure_id)')
                execute(db, 'add index idx_sa_source_id',     'CREATE INDEX idx_sa_structure_source_id ON ' + props.schema + '.structure_aliases(source_id)')
                execute(db, 'add index idx_sa_alias_value',   'CREATE INDEX idx_sa_structure_alias_value ON ' + props.schema + '.structure_aliases(alias_value)')
                
                execute(db, "create table ${props.schema}.property_definitions",  """\
                    |CREATE TABLE ${props.schema}.property_definitions (
                    |  id SERIAL PRIMARY KEY,
                    |  source_id integer NOT NULL,
                    |  property_description TEXT NOT NULL,
                    |  property_uri TEXT,
                    |  external_id TEXT,
                    |  est_size INTEGER,
                    |  definition TEXT,
                    |  example TEXT,
                    |  CONSTRAINT fk_sp2sources FOREIGN KEY (source_id) REFERENCES ${props.schema}.sources(id) ON DELETE CASCADE,
                    |  CONSTRAINT uq_properties UNIQUE (source_id, id)
                    |)""".stripMargin())
                execute(db, 'add index idx_pd_source_id',   'CREATE INDEX idx_pd_source_id ON ' + props.schema + '.property_definitions(source_id)')
                execute(db, 'add index idx_pd_external_id', 'CREATE INDEX idx_pd_external_id ON ' + props.schema + '.property_definitions(external_id)')
                
                execute(db, "create table ${props.schema}.instances",  """\
                    |CREATE TABLE ${props.schema}.instances (
                    |  id SERIAL PRIMARY KEY,
                    |  source_id INTEGER NOT NULL,
                    |  structure_id INTEGER NOT NULL,
                    |  structure_definition TEXT,
                    |  description TEXT,
                    |  external_id TEXT,
                    |  instance_uri TEXT,
                    |  CONSTRAINT fk_inst2sources FOREIGN KEY (source_id) REFERENCES ${props.schema}.sources(id) ON DELETE CASCADE,
                    |  CONSTRAINT fk_inst2structures FOREIGN KEY (structure_id) REFERENCES ${props.schema}.structures(cd_id) ON DELETE CASCADE
                    |)""".stripMargin())               
                execute(db, 'add index idx_inst_source_id', 'CREATE INDEX idx_inst_source_id ON ' + props.schema + '.instances(source_id)')
                execute(db, 'add index idx_inst_structure_id', 'CREATE INDEX idx_inst_structure_id ON ' + props.schema + '.instances(structure_id)')
                execute(db, 'add index idx_inst_external_id', 'CREATE INDEX idx_inst_external_id ON ' + props.schema + '.instances(external_id)')
 
                execute(db, "create table ${props.schema}.structure_props",  """\
                    |create table ${props.schema}.structure_props (
                    |  id SERIAL PRIMARY KEY,
                    |  structure_id INTEGER NOT NULL,
                    |  instance_id INTEGER,
                    |  property_def_id INTEGER NOT NULL,
                    |  property_data JSONB,
                    |  CONSTRAINT fk_sp2structures FOREIGN KEY (structure_id) references ${props.schema}.structures(cd_id) ON DELETE CASCADE,
                    |  CONSTRAINT fk_sp2propdefs FOREIGN KEY (property_def_id) references ${props.schema}.property_definitions(id) ON DELETE CASCADE
                    |)""".stripMargin())
                execute(db, 'add index idx_sp_instance_id',     'CREATE INDEX idx_sp_instance_id on ' + props.schema + '.structure_props(id)')
                execute(db, 'add index idx_sp_property_def_id',  'CREATE INDEX idx_sp_property_def_id on ' + props.schema + '.structure_props(property_def_id)')
                //execute(db, 'add index idx_sp_property_data', 'CREATE INDEX idx_sp_property_data ON ' + props.schema + '.structure_props USING gin (property_data jsonb_ops)')
            }
            println "seeding ${props.schema}.categories"
            db.executeInsert('INSERT INTO ' + props.schema + '''.categories (category_name) VALUES
                    ('ACTIVITY_DATA'),('CALC_PROP'),('PHYSCHEM')''')
        }
    }
}
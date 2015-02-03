/*
 * Copyright (c) 1998-2014 ChemAxon Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * ChemAxon. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with ChemAxon.
 */

package test.compress;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import chemaxon.util.ConnectionHandler;
import chemaxon.util.SmilesCompressor;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Statement;

public class CompressionStat {

    public static void main(String args[]) throws Exception {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        connectionHandler.setPropertyTable("chemcentral.jchemproperties");
        connectionHandler.setUrl("jdbc:postgresql://localhost:49153/chemcentral");
        connectionHandler.setLoginName("chemcentral");
        connectionHandler.setPassword("chemcentral");
        connectionHandler.setDriver("org.postgresql.Driver");
        connectionHandler.connectToDatabase();
        
        FileWriter writer = new FileWriter("/home/timbo/compressionstats-structures.csv");

        Connection con =  connectionHandler.getConnection();
        con.setAutoCommit(false);
        Statement stmt =con.createStatement();
        stmt.setFetchSize(10000);
        ResultSet resultSet = stmt.executeQuery("select cd_id, cd_smiles from chemcentral.structures");

        SmilesCompressor smilesCompressor = new SmilesCompressor();
        /* Some warmup ..*/
        for (int i = 0; i < 100000; i++) {
            byte[] compress = smilesCompressor.compress("CCCCNOC1CCCCCC1CCOOO(COOO)OCO");
        }
        writer.append("CD_ID, UNCOMPRESSED LENGTH, COMPRESSED LENGTH, COMPRESSION TIME(ns)\n");
        int count = 0;
        while (resultSet.next()) {
            count++;
            if (count % 10000 == 0) {
                System.out.println("Processed " + count);
            }
            int cd_id = resultSet.getInt("cd_id");
            String smiles = resultSet.getString("cd_smiles");
            if (smiles != null) {
                long start = System.nanoTime();
                byte[] compressed = smilesCompressor.compress(smiles);
                long end = System.nanoTime();
                String line = MessageFormat.format("{0}, {1}, {2}, {3}\n", cd_id, smiles.getBytes().length,
                        compressed.length, end - start);
                writer.append(line);
            }
        }
        writer.close();
    }
}
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

package org.squonk.cdk.services;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.squonk.cdk.io.CDKMolDepict;
import org.squonk.cdk.io.CDKMoleculeIOUtils;
import org.squonk.io.DepictionParameters;
import org.squonk.types.MoleculeObject;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mol depiction servlet using CDK.
 * Example URL: /moldepict?format=_format_&w=_width_&h=_height_&bg=_rgba_&expand=_expand_&mol=_smiles_
 * where:
 * <ul>
 * <li>_format_ is the output format, currently either png or svg</li>
 * <li>_width_ is the image width</li>
 * <li>_height_ is the image height</li>
 * <li>_rgba_ is the background color as RGBA integer (#AARRGGBB)</li>
 * <li>_expand_ is whether to expand the rendering to fit the image size (true/false)</li>
 * <li>_smiles_ is the molecule in some format that is recognised such as smiles</li>
 * </ul>
 * Only the format and mol parameters are required. Defaults will be used for the others if not specified.<br>
 * For example, this renders caffeine as SVG with a partly transparent yellow background (# is encoded as %23):<br>
 * http://192.168.99.100:8888/cdk_basic_services/moldepict?format=svg&w=75&h=75&bg=0x33FFFF00&mol=CN1C%3DNC2%3DC1C(%3DO)N(C)C(%3DO)N2C
 * <p>
 * POST operations are also supported with the body containing the molecule to render and the depiction params
 * specified as query parameters as described above.
 * <p>
 * Created by timbo on 24/01/2016.
 */
@WebServlet(
        name = "CDKMolConvertServlet",
        description = "Molecule conversion using CDK",
        urlPatterns = {"/molconvert"}
)
public class CdkMolConvertServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CdkMolConvertServlet.class.getName());


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        InputStream is = req.getInputStream();
        if (is == null) {
            return;
        }
        String mol = IOUtils.convertStreamToString(is, 1000);
        if (mol == null || mol.length() == 0) {
            return;
        }
        doConvert(req, resp, mol);
    }


    private void doConvert(HttpServletRequest req, HttpServletResponse resp, String mol) throws IOException {

        String format = req.getParameter("format");
        if (format == null) {
            LOG.info("No format specified. Cannot convert");
            return;
        }

        try {
            IAtomContainer convertedMol = CDKMoleculeIOUtils.readMolecule(mol);
            MoleculeObject convertedMO = CDKMoleculeIOUtils.convertToFormat(convertedMol, format, null);
            String convertedStr = convertedMO.getSource();
            byte[] bytes = convertedStr.getBytes();


            if (bytes != null) {
                // TODO should we return a media type specific to the format?
                resp.setHeader("Content-Type", CommonMimeTypes.MIME_TYPE_TEXT_PLAIN);
                resp.setHeader("Content-Length", "" + bytes.length);

                resp.getOutputStream().write(bytes);
                resp.getOutputStream().flush();
                resp.getOutputStream().close();
            }
        } catch (CDKException e) {
            throw new IOException("Failed to convert", e);
        }
    }

}

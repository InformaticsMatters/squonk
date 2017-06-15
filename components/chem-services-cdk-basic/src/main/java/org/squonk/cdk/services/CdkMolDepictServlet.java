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

import org.squonk.cdk.io.CDKMolDepict;
import org.squonk.io.DepictionParameters;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.BufferedReader;
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
        name = "CDKMolDepictServlet",
        description = "Molecule depiction using CDK",
        urlPatterns = {"/moldepict"}
)
public class CdkMolDepictServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CdkMolDepictServlet.class.getName());
    private final CDKMolDepict moldepict = new CDKMolDepict();  // with default params

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
        generateImage(req, resp, mol);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String paramMol = req.getParameter(DepictionParameters.PROP_MOL);
        if (paramMol == null) {
            LOG.info("No molecule specified. Cannot render");
            return;
        }
        generateImage(req, resp, paramMol);
    }

    private void generateImage(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mol) throws IOException {

        String paramFormat = req.getParameter(DepictionParameters.PROP_IMG_FORMAT);
        if (paramFormat == null) {
            LOG.info("No format specified. Cannot render");
            return;
        }

        DepictionParameters params = DepictionParameters.fromHttpParams(req.getParameterMap());

        if (DepictionParameters.IMG_FORMAT_PNG.equalsIgnoreCase(paramFormat)) {
            generatePng(req, resp, mol, params);
        } else if (DepictionParameters.IMG_FORMAT_SVG.equalsIgnoreCase(paramFormat)) {
            generateSvg(req, resp, mol, params);
        }
    }


    private void generatePng(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mol,
            DepictionParameters params) throws IOException {

        byte[] bytes = null;
        try {
            bytes = moldepict.stringToImage(mol, DepictionParameters.IMG_FORMAT_PNG.toUpperCase(), params);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error in png depiction", e);
            return;
        }
        if (bytes != null) {

            resp.setHeader("Content-Type",  CommonMimeTypes.MIME_TYPE_PNG);
            resp.setHeader("Content-Length", "" + bytes.length);

            resp.getOutputStream().write(bytes);
            resp.getOutputStream().flush();
            resp.getOutputStream().close();
        }
    }

    private void generateSvg(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mol,
            DepictionParameters params) throws IOException {

        String svg = null;
        try {
            svg = moldepict.stringToSVG(mol, params);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error in svg depiction", e);
            return;
        }
        if (svg != null) {
            resp.setHeader("Content-Type", CommonMimeTypes.MIME_TYPE_SVG);
            resp.setHeader("Content-Length", "" + svg.getBytes().length);

            resp.getWriter().print(svg);
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }



}

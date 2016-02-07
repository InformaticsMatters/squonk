package org.squonk.cdk.services;

import org.squonk.cdk.io.CDKMolDepict;
import org.squonk.io.DepictionParameters;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Mol depiction servlet using CDK.
 * Example URL: /moldepict?format=_format_&w=_width_&h=_height_&bg=_rgba_&expand=_expand_&mol=_smiles_
 * where:
 * <ul>
 *     <li>_format_ is the output format, currently either png or svg</li>
 *     <li>_width_ is the image width</li>
 *     <li>_height_ is the image height</li>
 *     <li>_rgba_ is the background color as RGBA integer (#AARRGGBB)</li>
  *     <li>_expand_ is whether to expand the rendering to fit the image size (true/false)</li>
 *     <li>_smiles_ is the molecule in some format that is recognised such as smiles</li>
 * </ul>
 * Only the format and mol parameters are required. Defaults will be used for the others if not specified.<br>
 * For example, this renders caffeine as SVG with a partly transparent yellow background (# is encoded as %23):<br>
 * http://192.168.99.100:8888/cdk_basic_services/moldepict?format=svg&w=75&h=75&bg=0x33FFFF00&mol=CN1C%3DNC2%3DC1C(%3DO)N(C)C(%3DO)N2C
 *
 * Created by timbo on 24/01/2016.
 */
@WebServlet(
        name = "CDKMolDepictServlet",
        description = "Moleucle depiction using CDK",
        urlPatterns = {"/moldepict"}
)
public class CdkMolDepictServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CdkMolDepictServlet.class.getName());
    private final CDKMolDepict moldepict = new CDKMolDepict();  // with default params

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String paramMol = req.getParameter("mol");
        if (paramMol == null) {
            LOG.info("No molecule specified. Cannot render");
            return;
        }

        String paramFormat = req.getParameter("format");
        if (paramFormat == null) {
            LOG.info("No format specified. Cannot render");
            return;
        }

        DepictionParameters params = createDepictionParams(req);

        if ("png".equalsIgnoreCase(paramFormat)) {
            generatePng(req, resp, paramMol, params);
        } else if ("svg".equalsIgnoreCase(paramFormat)) {
            generateSvg(req, resp, paramMol, params);
        }

    }

    private void generatePng(
            HttpServletRequest req,
            HttpServletResponse resp,
            String mol,
            DepictionParameters params) throws IOException {

        byte[] bytes = null;
        try {
            bytes = moldepict.stringToImage(mol, "PNG", params);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error in png depiction", e);
            return;
        }
        if (bytes != null) {

            resp.setHeader("Content-Type", "image/png");
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
            resp.setHeader("Content-Type", "image/svg+xml");
            resp.setHeader("Content-Length", "" + svg.getBytes().length);

            resp.getWriter().print(svg);
            resp.getWriter().flush();
            resp.getWriter().close();
        }
    }


    private DepictionParameters createDepictionParams(HttpServletRequest req) {
        String paramWidth = req.getParameter("w");
        String paramHeight = req.getParameter("h");
        String paramExpand = req.getParameter("expand");
        String paramBg = req.getParameter("bg");
        String paramAlpha = req.getParameter("alpha");

        // size
        Integer width = null;
        Integer height = null;
        if (paramWidth != null && paramHeight != null) {
            try {
                width = Integer.parseInt(paramWidth);
                height = Integer.parseInt(paramHeight);

            } catch (NumberFormatException ex) {
                LOG.log(Level.INFO, "Can't interpret expand parameters: " + paramWidth + " " + paramHeight, ex);
            }
        }

        // background
        Color col = null;
        if (paramBg != null || paramAlpha != null) {
            try {

                col = new Color(Long.decode(paramBg).intValue(), true);

//                col = Color.decode(paramBg == null ? "#FFFFFF" : paramBg);
//                if (paramAlpha != null) {
//                    int alpha = Integer.decode(paramAlpha);
//                    col = new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
//                }
            } catch (NumberFormatException ex) {
                LOG.log(Level.INFO, "Can't interpret color parameters: " + paramBg, ex);
            }
        }

        // expand to fit
        Boolean expand = true;
        if (paramExpand != null) {
            try {
                expand = Boolean.parseBoolean(paramExpand);
            } catch (Exception ex) {
                LOG.log(Level.INFO, "Can't interpret expand parameter: " + paramExpand, ex);
            }
        }

        return (expand != null || width != null || height != null || col != null) ?
                new DepictionParameters(width, height, expand, col) : null;
    }

}

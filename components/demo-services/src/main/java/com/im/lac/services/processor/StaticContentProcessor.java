package com.im.lac.services.processor;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/**
 * Serves up static web content. Based on
 * <a href="http://code.notsoclever.cc/writing-camel-component-camel-static-resource/">this
 * example </a>
 *
 * @author timbo
 */
public class StaticContentProcessor implements Processor {

    String root;

    public StaticContentProcessor(String root) {
        this.root = root;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();

        String relativepath = in.getHeader(Exchange.HTTP_PATH, String.class);

        if (relativepath.isEmpty()) {
            relativepath = "index.html";
        }

        String pathStr = root + relativepath;

        Path path = FileSystems.getDefault().getPath(pathStr);
        String mimeType = Files.probeContentType(path);

        Message out = exchange.getOut();
        try {
            out.setBody(Files.readAllBytes(path));
            out.setHeader(Exchange.CONTENT_TYPE, mimeType);
        } catch (IOException e) {
            out.setBody(pathStr + " not found.");
            out.setHeader(Exchange.HTTP_RESPONSE_CODE, "404");
        }
    }
}

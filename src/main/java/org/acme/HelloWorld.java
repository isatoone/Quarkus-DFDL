package org.acme;

import org.apache.daffodil.japi.*;
import org.apache.daffodil.japi.infoset.JDOMInfosetOutputter;
import org.apache.daffodil.japi.infoset.JsonInfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.filter.ContentFilter;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformException;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates using the Daffodil DFDL processor to
 * <ul>
 * <li>compile a DFDL schema
 * <li>parse non-XML data into XML,
 * <li>access the data it using XPath,
 * <li>transform the data using XSLT
 * <li>unparse the transformed data back to non-XML form.
 * </ul>
 */
public class HelloWorld {

    public void start() throws IOException, XSLTransformException, URISyntaxException {
        try(FileOutputStream outputStream = new FileOutputStream("test.txt")) {

            URI schemaFileURI = new File("files/helloWorld.dfdl.xsd").toURI();
            URI dataFileURI = new File("files/helloWorld.dat").toURI();
            URI xsltFileURI = new File("files/helloWorld.xslt").toURI();

            outputStream.write(("Schéma : " + schemaFileURI + "\n").getBytes());
            outputStream.write(("Data : " + dataFileURI + "\n").getBytes());
            outputStream.write(("XSLT : " + xsltFileURI + "\n").getBytes());
            String fileToLoad = System.getProperty("test.file.toload");
            outputStream.write(("Property : " + fileToLoad + "\n").getBytes());

            //
            // Run the executable with -Dtest.file.toload=path/to/resource/file to test if the file is included in the executable
            //
            if(fileToLoad != null) {
                URL resourceURL = HelloWorld.class.getResource(fileToLoad);
                if(resourceURL != null) {
                    outputStream.write(("Found resource URL : " + resourceURL + "\n").getBytes());
                    outputStream.write(("Found resource URI : " + resourceURL.toURI() + "\n").getBytes());
                    try(InputStream inputStream = resourceURL.openStream()) {
                        outputStream.write(inputStream.readAllBytes());
                    }
                }
            }
            //
            // First compile the DFDL Schema
            //
            Compiler c = Daffodil.compiler();
            ProcessorFactory pf = c.compileSource(schemaFileURI);
            outputStream.write("XSD compiled\n".getBytes());
            List<Diagnostic> diags1 = pf.getDiagnostics();
            outputStream.write("Diagnostics : \n".getBytes());
            for(Diagnostic d : diags1) {
                outputStream.write(d.getSomeMessage().getBytes());
                outputStream.write("\n".getBytes());
            }

            if(pf.isError()) {
                // didn't compile schema. Must be diagnostic of some sort.
                List<Diagnostic> diags = pf.getDiagnostics();
                for(Diagnostic d : diags) {
                    outputStream.write(d.getSomeMessage().getBytes());
                    outputStream.write("\n".getBytes());
                }
            }

            DataProcessor dp = pf.onPath("/");
            if(dp.isError()) {
                // didn't compile schema. Must be diagnostic of some sort.
                List<Diagnostic> diags = dp.getDiagnostics();
                for(Diagnostic d : diags) {
                    outputStream.write(d.getSomeMessage().getBytes());
                    outputStream.write("\n".getBytes());
                }
            }
            //
            // Parse - parse data to XML
            //
            outputStream.write("**** Parsing data into XML *****".getBytes());
            outputStream.write("\n".getBytes());
            java.io.InputStream is = dataFileURI.toURL().openStream();
            InputSourceDataInputStream dis = new InputSourceDataInputStream(is);
            //
            // Setup JDOM outputter
            //
            JDOMInfosetOutputter outputter = new JDOMInfosetOutputter();

            // Do the parse
            //
            ParseResult res = dp.parse(dis, outputter);

            // Check for errors
            //
            boolean err = res.isError();
            if(err) {
                // didn't parse the data. Must be diagnostic of some sort.
                List<Diagnostic> diags = res.getDiagnostics();
                for(Diagnostic d : diags) {
                    outputStream.write(d.getSomeMessage().getBytes());
                    outputStream.write("\n".getBytes());
                }

            }

            Document doc = outputter.getResult();
            //
            // if we get here, we have a parsed infoset result!
            // Let's print the XML infoset.
            //
            // Note that if we had only wanted this text, we could have used
            // a different outputter to create XML text directly,
            // but below we're going to transform this JDOM tree.
            //
            XMLOutputter xo = new XMLOutputter(org.jdom2.output.Format.getPrettyFormat());
            xo.output(doc, outputStream);
            outputStream.write("\n".getBytes());

            // If all you need to do is parse things to XML, then that's it.

            // Let's display it as JSON also for those that need or prefer JSON
            {
                outputStream.write("**** Parsing data into JSON ****".getBytes());
                outputStream.write("\n".getBytes());
                java.io.InputStream is2 = dataFileURI.toURL().openStream();
                InputSourceDataInputStream dis2 = new InputSourceDataInputStream(is2);
                JsonInfosetOutputter jo = new JsonInfosetOutputter(outputStream, true);
                outputStream.write("\n".getBytes());
                ParseResult res2 = dp.parse(dis2, jo);
                boolean err2 = res2.isError();
                if(err2) {
                    // didn't parse the data. Must be diagnostic of some sort.
                    List<Diagnostic> diags = res.getDiagnostics();
                    for(Diagnostic d : diags) {
                        outputStream.write(d.getSomeMessage().getBytes());
                        outputStream.write("\n".getBytes());
                    }

                }
            }
            //
            // XPATH - use it to access the data
            //
            outputStream.write("**** Access with XPath *****".getBytes());
            outputStream.write("\n".getBytes());

            XPathExpression<Content> xexp = setupXPath("/hw:helloWorld/word[2]/text()");
            List<Content> clist = xexp.evaluate(doc);
            if(clist.isEmpty()) {
                outputStream.write("XPath produced nothing.".getBytes());
                outputStream.write("\n".getBytes());

            } else {
                Content content = clist.getFirst();
                String txt = content.getValue();
                outputStream.write(String.format("XPath says we said hello to %s%n", txt).getBytes());
                outputStream.write("\n".getBytes());
            }
            outputStream.write("End hello world".getBytes());
        }
    }

    /**
     * Does the boilerplate stuff needed for xpath expression setup
     *
     * @return the compiled XPathExpression object which can be evaluated to run it.
     */
    private static XPathExpression<Content> setupXPath(String xpath) {
        // Need this namespace definition since the schema defines the root
        // element in this namespace.
        //
        // A real application would hoist this boilerplate all out so it's done
        // once, not each time we need to evaluate an XPath expression.
        //
        Namespace[] nss = {Namespace.getNamespace("hw", "http://example.com/dfdl/helloworld/")};
        XPathFactory xfactory = XPathFactory.instance();
        ContentFilter cf = new ContentFilter(ContentFilter.TEXT);
        Map<String, Object> variables = Collections.emptyMap();
        return xfactory.compile(xpath, cf, variables, nss);
    }
}
package edu.brandeis.lapps.stanford.corenlp;

import junit.framework.Assert;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * <i>TestParser.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 *
 */
public class TestDependencyParser extends TestService {

    String testSent = "Hi, Programcreek is a very huge and useful website.";

    public TestDependencyParser() {
        service = new DependencyParser();
    }


    @Test
    public void testMetadata() {
        ServiceMetadata metadata = super.testCommonMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals("Expected 3 annotations, found: " + produces.getAnnotations().size(),
                3, produces.getAnnotations().size());
        assertTrue("Tokens not produced",
                produces.getAnnotations().contains(Uri.TOKEN));
        assertTrue("Dependencies not produced",
                produces.getAnnotations().contains(Uri.DEPENDENCY));
        assertTrue("Dependency Structures not produced",
                produces.getAnnotations().contains(Uri.DEPENDENCY_STRUCTURE));
    }

    @Test
    public void testExecute(){


        String result0 = service.execute(testSent);
        String input = new Data<>(Uri.LIF, wrapContainer(testSent)).asJson();
        String result = service.execute(input);
        Assert.assertEquals(result0, result);
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------------------------------------>");


        input = "Hi, Programcreek is a very huge and useful website.";
        result = service.execute(input);
        Container resultContainer = reconstructPayload(result);
        assertEquals("Text is corrupted.", resultContainer.getText(), testSent);
        List<View> views = resultContainer.getViews();
        if (views.size() != 1) {
            fail(String.format("Expected 1 view. Found: %d", views.size()));
        }
        View view = resultContainer.getView(0);
        assertTrue("Not containing tokens", view.contains(Uri.TOKEN));
        assertTrue("Not containing dependency", view.contains(Uri.DEPENDENCY));
        assertTrue("Not containing dependency structure", view.contains(Uri.DEPENDENCY_STRUCTURE));
        System.out.println(Serializer.toPrettyJson(resultContainer));
    }
}

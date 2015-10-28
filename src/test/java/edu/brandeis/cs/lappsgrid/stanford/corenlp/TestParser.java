package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import junit.framework.Assert;
import org.junit.Test;
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
 * <p> Test cases are from
 * <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">
 *     OpenNLP Tutorial</a>
 * <p>
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 *
 */
public class TestParser extends TestService {

    String testSent = "Programcreek is a very huge and useful website.";

    public TestParser() throws StanfordWebServiceException {
        service = new Parser();
    }

    @Test
    public void testExecute(){
        String input = new Data<>(Uri.LIF, wrapContainer(testSent)).asJson();
        String result = service.execute(input);
        Container resultContainer = reconstructPayload(result);
        assertEquals("Text is corrupted.", resultContainer.getText(), testSent);
        List<View> views = resultContainer.getViews();
        if (views.size() != 1) {
            fail(String.format("Expected 1 view. Found: %d", views.size()));
        }
        View view = resultContainer.getView(0);
        assertTrue("Not containing tokens", view.contains(Uri.TOKEN));
        assertTrue("Not containing constituents", view.contains(Uri.CONSTITUENT));
        assertTrue("Not containing phrase structure", view.contains(Uri.PHRASE_STRUCTURE));
        System.out.println(Serializer.toPrettyJson(resultContainer));
    }

    @Test
    public void testExecuteText(){



        String result0 = service.execute(testSent);
        String input = new Data<>(Uri.LIF, wrapContainer(testSent)).asJson();
        String result = service.execute(input);
        Assert.assertEquals(result0, result);
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------------------------------------>");



//        String input = new Data<>(Uri.LIF, wrapContainer(testSent)).asJson();
        String lifResult = Serializer.toJson(
                reconstructPayload(service.execute(input)));
        input = new Data<>(Uri.TEXT, testSent).asPrettyJson();
        String textResult = Serializer.toJson(
                reconstructPayload(service.execute(input)));
        assertEquals("Results are not matching", lifResult, textResult);
    }

}

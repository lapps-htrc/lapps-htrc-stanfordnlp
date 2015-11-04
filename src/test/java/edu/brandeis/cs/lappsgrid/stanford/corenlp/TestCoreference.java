package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
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
 * <i>TestTokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 *
 */
public class TestCoreference extends TestService {

    String testSent= "Mike, Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.";

    public TestCoreference() throws StanfordWebServiceException {
        service = new Coreference();
    }


    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testCommonMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals(
                "Expected 3 annotations, found: " + produces.getAnnotations().size(),
                3, produces.getAnnotations().size());
        assertTrue("Tokens not produced",
                produces.getAnnotations().contains(Uri.SENTENCE));
        assertTrue("Markabels not produced",
                produces.getAnnotations().contains(Uri.MARKABLE));
        assertTrue("Coreference chains not produced",
                produces.getAnnotations().contains(Uri.COREF));
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

        Container resultContainer = reconstructPayload(result);

        assertEquals("Text is corrupted.", resultContainer.getText(), testSent);
        List<View> views = resultContainer.getViews();
        if (views.size() != 1) {
            fail(String.format("Expected 1 view. Found: %d", views.size()));
        }
        View view = resultContainer.getView(0);
        assertTrue("Not containing tokens", view.contains(Uri.TOKEN));
        assertTrue("Not containing markables", view.contains(Uri.MARKABLE));
        assertTrue("Not containing coreference chains", view.contains(Uri.COREF));
        System.out.println(Serializer.toPrettyJson(resultContainer));
    }
}

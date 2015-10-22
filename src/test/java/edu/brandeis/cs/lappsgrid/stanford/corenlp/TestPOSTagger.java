package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import org.junit.Test;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * <i>TestPOSTagger.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 *
 */
public class TestPOSTagger extends TestService {

    String testSent = "Good morning.";

    public TestPOSTagger() throws StanfordWebServiceException {
        service = new POSTagger();
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
        assertTrue("Not containing POS tags", view.contains(Uri.POS));
        List<Annotation> annotations = view.getAnnotations();
        if (annotations.size() != 3) {
            fail(String.format("Expected 3 tokens. Found: %d", views.size()));
        }
        Annotation annotation = annotations.get(0);
        assertEquals("@type is not correct: " + annotation.getAtType(),
                annotation.getAtType(), Uri.POS);
        String goodPos = annotation.getFeature("pos");
        assertEquals("'Good' is a JJ.Found: " + goodPos, goodPos, "JJ");
        System.out.println(Serializer.toPrettyJson(resultContainer));
    }
}
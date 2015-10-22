package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import org.junit.Test;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * <i>TestTokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p>
 * <p>
 * Test cases are from <a
 * href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP
 * Tutorial</a>
 * <p>
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>
 *         Nov 20, 2013<br>
 *
 */
public class TestNamedEntityRecognizer extends TestService {

    String testSent = "Hello Mike.";

    public TestNamedEntityRecognizer() throws StanfordWebServiceException {
        service = new NamedEntityRecognizer();
    }

    @Test
    public void testMetadata() {
        Data data = Serializer.parse(service.getMetadata(), Data.class);
        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
        assertEquals("Name is not correct",
                NamedEntityRecognizer.class.getName(), metadata.getName());
    }

    @Test
    public void testExecute() {
        String input = new Data<>(Uri.LIF, wrapContainer(testSent)).asJson();
        String result = service.execute(input);
        Container resultContainer = reconstructPayload(result);
        assertEquals("Text is corrupted.", resultContainer.getText(), testSent);
        List<View> views = resultContainer.getViews();
        if (views.size() != 1) {
            fail(String.format("Expected 1 view. Found: %d", views.size()));
        }
        View view = resultContainer.getView(0);
        assertTrue("Not containing named entities", view.contains(Uri.NE));
        List<Annotation> annotations = view.getAnnotations();
        if (annotations.size() != 1) {
            fail(String.format("Expected 1 NE. Found: %d", views.size()));
        }
        Annotation mike = annotations.get(0);
        assertEquals("Mike is a person. @type is not correct: " + mike.getAtType(),
                mike.getAtType(), Uri.PERSON);
        System.out.println(Serializer.toPrettyJson(resultContainer));
    }
}



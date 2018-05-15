package edu.brandeis.lapps.stanford.corenlp;

import junit.framework.Assert;
import org.junit.Test;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
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
public class TestRelationExtractor extends TestService {


    public TestRelationExtractor() {
        service = new RelationExtractor();
    }


    @Test
    public void testMetadata(){
        ServiceMetadata metadata = super.testCommonMetadata();
        IOSpecification requires = metadata.getRequires();
        IOSpecification produces = metadata.getProduces();
        assertEquals(
                "Expected 2 annotations, found: " + produces.getAnnotations().size(),
                2, produces.getAnnotations().size());
        assertTrue("Markabels not produced",
                produces.getAnnotations().contains(Uri.MARKABLE));
        assertTrue("Coreference chains not produced",
                produces.getAnnotations().contains(Uri.GENERIC_RELATION));
    }

    @Test
    public void testExecute(){
        String testSent= "Nancy flew to New York. she lives in Manhattan.";
        String result0 = service.execute(testSent);
        String input = new Data<>(Uri.LIF, wrapContainer(testSent)).asJson();
        String result = service.execute(input);
        Assert.assertEquals(result0, result);

        Container resultContainer = reconstructPayload(result);

        assertEquals("Text is corrupted.", resultContainer.getText(), testSent);
        List<View> views = resultContainer.getViews();
        if (views.size() != 1) {
            fail(String.format("Expected 1 view. Found: %d", views.size()));
        }
        View view = resultContainer.getView(0);
        assertTrue("Not containing markables", view.contains(Uri.MARKABLE));
        assertTrue("Not containing relations", view.contains(Uri.GENERIC_RELATION));
//        System.out.println(Serializer.toPrettyJson(resultContainer));
        // fly-to, live-in
        long numRelations = view.getAnnotations().stream().filter(annotation -> annotation.getAtType().equals(Uri.GENERIC_RELATION)).count();
        assertEquals("Expected 2 relations", 2, numRelations);
        // nancy, new-york, she, manhattan + 2 relations
        long numMarkables = view.getAnnotations().stream().filter(annotation -> annotation.getAtType().equals(Uri.MARKABLE)).count();
        assertEquals("Expected 6 markables", 6, numMarkables);
        assertEquals("Expected total 8 annotations", 8, view.getAnnotations().size());

    }
}

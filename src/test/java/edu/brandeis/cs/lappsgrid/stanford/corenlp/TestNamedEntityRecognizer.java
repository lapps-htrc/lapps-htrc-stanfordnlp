package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import org.junit.Assert;
import org.junit.Test;
import static org.lappsgrid.discriminator.Discriminators.*;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.Map;

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
public class TestNamedEntityRecognizer {

    NamedEntityRecognizer ner;

    public TestNamedEntityRecognizer() throws StanfordWebServiceException {
        ner = new NamedEntityRecognizer();
    }

    @Test
    public void testFind() {
        String text = "Mike, Smith is a good person and he is from Boston.";
        String ners = ner.find(text);
        Assert.assertEquals(
                "NamedEntityRecognizer Failure.",
                ners,
                "<PERSON>Mike</PERSON> , <PERSON>Smith</PERSON> is a good person and he is from <LOCATION>Boston</LOCATION> .");
    }

    @Test
    public void testMetadata() {
        Data data = Serializer.parse(ner.getMetadata(), Data.class);
        System.out.println(data.asPrettyJson());
        System.out.println(data.getPayload().getClass());
        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
        Assert.assertEquals(
                "Name is not correct",
                NamedEntityRecognizer.class.getName(), metadata.getName()
        );
    }



    @Test
    public void testExecute(){
        // TODO complete here

        String text = "Hello Mike.";
        Data data = Serializer.parse(ner.getMetadata(), Data.class);
        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
        Container container = new Container();
        container.setText(text);
        container.setLanguage("en");
//        container.setMetadata(metadata);
        String result = ner.execute(new Data<Container>(Uri.LIF, container).asPrettyJson());
        System.out.println(result);
        assert true;
    }

}

package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Coreference;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.Map;


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

    Coreference coref;

    public TestCoreference() throws StanfordWebServiceException {
        coref = new Coreference();
    }

    @Test
    public void testExecute(){
        // TODO 151017 complete here
//        String text = "Sue see herself.";
        String text = "Mike, Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.";
        Data data = Serializer.parse(coref.getMetadata(), Data.class);
        ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
        Container container = new Container();
        container.setText(text);
        container.setLanguage("en");
        container.setMetadata((Map) data.getPayload());
        String result = coref.execute(new Data<>(Discriminators.Uri.LIF, container).asPrettyJson());
        System.out.println(result);

//        json = coref.execute("Mike, Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.");
//        System.out.println(json);
//        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());
    }
}

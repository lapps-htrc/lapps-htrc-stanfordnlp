package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Coreference;
import org.junit.Test;
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
	@Test
	public void testCoref() {

	}

    @Test
    public void testExecute(){
		Coreference coref = new Coreference();
		String json = coref.execute("Sue see herself.");
		System.out.println(json);
		Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


		json = coref.execute("Mike, Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.");
		System.out.println(json);
		container = new Container((Map) Serializer.parse(json, Data.class).getPayload());
    }
}

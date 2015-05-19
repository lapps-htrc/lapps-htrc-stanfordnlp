package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import org.junit.Assert;
import org.junit.Test;
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
public class TestNamedEntityRecognizer extends TestService {

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
    public void testExecute(){

        System.out.println("/-----------------------------------\\");

        String json = ner.execute("Mike");
        System.out.println(json);
        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute("Hello Mike");
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("payload1.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("tokens.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


        System.out.println("\\-----------------------------------/\n");
    }

}

package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.Map;

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
	
	POSTagger postagger;

	public TestPOSTagger() throws StanfordWebServiceException {
		postagger = new POSTagger();
	}
	
	@Test
	public void testTokenize() {
		String docs = "Hi. How are you? This is Mike.";
		String[] tags = postagger.tag(docs);
		String [] goldTags = {"NN", ".", "WRB", "VBP", "PRP", ".", "DT", "VBZ", "NNP", "."};
		Assert.assertArrayEquals("Tokenize Failure.", goldTags, tags);
	}

    @Test
    public void testExecute(){{

        System.out.println("/-----------------------------------\\");

        String json = postagger.execute("Good");
        System.out.println(json);
        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


        json = postagger.execute("Good Morning");
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("payload1.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("tokens.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


        System.out.println("\\-----------------------------------/\n");
    }
    }




}

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
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestTokenizer extends TestService {
	
	Tokenizer tokenizer;
	
	public TestTokenizer() throws StanfordWebServiceException {
		tokenizer = new Tokenizer();
	}
	
	@Test
	public void testTokenize() {
		String [] tokens = tokenizer.tokenize("Hi. How are you? This is Mike.");
//		System.out.println(Arrays.toString(tokens));
		String [] goldTokens = {"Hi",".","How","are","you","?","This","is","Mike","."};
		Assert.assertArrayEquals("Tokenize Failure.", goldTokens, tokens);
	}

    @Test
    public void testExecute(){
        System.out.println("/-----------------------------------\\");
        String json = tokenizer.execute(jsons.get("payload1.json"));
        System.out.println(json);

        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = tokenizer.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = tokenizer.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = tokenizer.execute(jsons.get("splitter.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        System.out.println("\\-----------------------------------/\n");
    }
}

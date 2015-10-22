package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import org.junit.Assert;
import org.junit.Test;

/**
 * <i>TestSplitter.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestSplitter extends TestService {
	
	Splitter splitter;
	
	public TestSplitter() throws StanfordWebServiceException {
		splitter = new Splitter();
	}
	
	@Test
	public void testSplit() {
		String [] sents = splitter.split("Hi. How are you? This is Mike.");
//		System.out.println(Arrays.toString(sents));
		String [] goldSents = {"Hi.","How are you?","This is Mike."};
		Assert.assertArrayEquals("Splitter Failure.", goldSents, sents);
	}


    @Test
    public void testExecute(){
//        ret = splitter.execute(data);
//        Assert.assertTrue(ret.getPayload().contains("by return email or by telephone"));

        /*
        System.out.println("/-----------------------------------\\");
        String json = splitter.execute(jsons.get("payload1.json"));
        System.out.println(json);
        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = splitter.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = splitter.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());
        System.out.println("\\-----------------------------------/\n");
        */
    }
}

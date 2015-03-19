package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Coreference;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Tokenizer;
import org.junit.Assert;
import org.junit.Test;


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

	Coreference tokenizer;

	public TestCoreference() throws StanfordWebServiceException {
		tokenizer = new Coreference();
	}
	
	@Test
	public void testCoref() {
	}

    @Test
    public void testExecute(){
//        ret = tokenizer.execute(data);
//        Assert.assertTrue(ret.getPayload().contains("by return email or by telephone"));
//        System.out.println(ret.getPayload());
    }
}

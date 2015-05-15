package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.DependencyParser;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.Parser;
import org.junit.Assert;
import org.junit.Test;

/**
 * <i>TestParser.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestDependencyParser extends TestService {

	DependencyParser parser;

	public TestDependencyParser() throws StanfordWebServiceException {
		parser = new DependencyParser();
	}
	
	@Test
	public void testParser() {
		String print = parser.parse("Programcreek is a very huge and useful website.");
		System.out.println(print);
	}


    @Test
    public void testExecute(){
        String ret = parser.execute("Programcreek is a very huge and useful website.");
		System.out.println(ret);
//        Assert.assertTrue(ret.getPayload().contains("NN"));
//        Assert.assertTrue(ret.getPayload().contains("by return email or by telephone"));
    }

}

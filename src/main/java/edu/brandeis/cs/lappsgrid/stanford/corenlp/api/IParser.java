package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

/**
 * <i>IParser.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> <a href="http://nlp.stanford.edu/software/corenlp.shtml">Parser</a>
 * <p> 
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface IParser {

	public String parse(String sentence);
}

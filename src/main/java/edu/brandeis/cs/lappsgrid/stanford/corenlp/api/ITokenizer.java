package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

/**
 * <i>ITokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p><a href="http://nlp.stanford.edu/software/corenlp.shtml">Tokenizer</a> 
 * <p> 
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface ITokenizer  {
	public String[] tokenize(String s);
}

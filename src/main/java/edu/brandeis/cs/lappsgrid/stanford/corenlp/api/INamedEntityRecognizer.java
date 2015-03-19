package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;


/**
 * <i>INamedEntityRecognizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> <a href="http://nlp.stanford.edu/software/corenlp.shtml">Named Entity Recognition</a>
 * <p> 
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface INamedEntityRecognizer {
	
	/**
	 *  Generates name tags for the given sequence, typically a sentence, returning token spans for any identified names.
	 * 
	 * @see{opennlp.tools.namefind.TokenNameFinder}
	 */
	public String find(String docs) ;
}

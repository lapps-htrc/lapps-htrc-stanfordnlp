package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

/**
 * <i>IPOSTagger.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p><a href="http://nlp.stanford.edu/software/corenlp.shtml">Part-of-Speech Tagger</a> 
 * <p>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface IPOSTagger {

	  /**
	   * Assigns the sentence of tokens pos tags.
	   * @param sentence The sentece of tokens to be tagged.
	   * @return an array of pos tags for each token provided in sentence.
	   */
	  public String[] tag(String document);
}

package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;

/**
 * <i>ISplitter.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p><a href="http://nlp.stanford.edu/software/corenlp.shtml">Sentence Detector</a> 
 * <p> 
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface ISplitter{
	public String [] split(String docs);
}

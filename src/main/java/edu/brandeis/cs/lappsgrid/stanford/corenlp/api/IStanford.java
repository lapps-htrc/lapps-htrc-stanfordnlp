package edu.brandeis.cs.lappsgrid.stanford.corenlp.api;


import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * <i>IOpenNLP.java</i> Language Application Grids (<b>LAPPS</b>)
 * 
 * <p> OpenNLP command line tool as Web service
 * 
 * <p><b>data</b>: MASC (optional GW, OANC)<br>
 *    <b>spliter</b>:OpenNLP,  <i><b>Stanford</b></i>, GATE<br>
 *    <b>tokenizer</b>: OpenNLP,  <i><b>Stanford</b></i>, GATE<br>
 *    <b>tagger</b>: OpenNLP,  <i><b>Stanford</b></i>, GATE<br>
 *    <b>parser</b>: OpenNLP,  <i><b>Stanford</b></i>, GATE NC+VC<br>
 *    <b>named-entity recoginzer (NER)</b>: OpenNLP, <i><b>Stanford</b></i>, Gate <br> </p>
 * <p><a href="http://nlp.stanford.edu/software/corenlp.shtml">Stanford Developer Document</a> 
 * <p>Implement interface
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 19, 2013<br>
 * 
 */
public interface IStanford {
	public static final String SPLITTER_LINE = "\n";
	
	/**
	 * chunkerMET
	 */
	public String[] chunkerMETArr(String[] lines) throws StanfordWebServiceException;
	
	/**
	 *
	 */
	public String[] chunkerMET(String lineswithsplitter) throws StanfordWebServiceException;
	
	/**
	 * Coreferencer
	 * @see opennlp.tools.cmdline.BasicCmdLineTool
	 * @see opennlp.tools.cmdline.coref.CoreferencerTool
	 */
//	public String[] coreferencerArr(String[] lines) throws OpenNLPWebServiceException;
//	
//	public String[] coreferencer(String lineswithsplitter) throws OpenNLPWebServiceException;
	
	/**
	 * DictionaryDetokenizer
	 * @see opennlp.tools.cmdline.BasicCmdLineTool
	 * @see opennlp.tools.cmdline.tokenizer.DictionaryDetokenizerTool
	 */
//	public String[] dictionaryDetokenizerArr(String[] lines) throws OpenNLPWebServiceException;
//	
//	public String[] dictionaryDetokenizer(String lineswithsplitter) throws OpenNLPWebServiceException;
	
	/**
	 * Doccat
	 * @see opennlp.tools.cmdline.BasicCmdLineTool
	 * @see opennlp.tools.cmdline.doccat.DoccatTool
	 */
//	public String[] doccatArr(String[] lines) throws OpenNLPWebServiceException;
//	
//	public String[] doccat(String lineswithsplitter) throws OpenNLPWebServiceException;
	
	/**
	 * Parser
	 */
	public String[] parserArr(String[] lines) throws StanfordWebServiceException;
	
	public String[] parser(String lineswithsplitter) throws StanfordWebServiceException;
	
	
	/**
	 * SentenceDetector
	 */
	public String[] sentenceDetectorArr(String[] lines) throws StanfordWebServiceException;
	
	public String[] sentenceDetector(String lineswithsplitter) throws StanfordWebServiceException;
	
	
	/**
	 * SimpleTokenizer
	 */
	public String[] simpleTokenizerArr(String[] lines) throws StanfordWebServiceException;
	
	public String[] simpleTokenizer(String lineswithsplitter) throws StanfordWebServiceException;
	
	/**
	 * TokenizerME
	 */
	public String[] tokenizerMEArr(String[] lines) throws StanfordWebServiceException;
	
	public String[] tokenizerME(String lineswithsplitter) throws StanfordWebServiceException;
	
	/**
	 * TokenNameFinder
	 */
	public String[] tokenNameFinderArr(String[] lines) throws StanfordWebServiceException;
	
	public String[] tokenNameFinder(String lineswithsplitter) throws StanfordWebServiceException;
}

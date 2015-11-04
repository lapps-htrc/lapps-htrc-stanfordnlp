package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
import edu.brandeis.cs.lappsgrid.stanford.corenlp.api.INamedEntityRecognizer;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 *
 * The Language Application Grid: A Framework for Rapid Adaptation and Reuse
 * <p>
 * Lapps Grid project TODO.
 * <p>
 * @author Chunqi SHI (shicq@cs.brandeis.edu) <br> Jan 31, 2014 </br>
 *
 */

@org.lappsgrid.annotations.ServiceMetadata(
        description = "Stanford CoreNLP Named Entity Recognizer",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "person", "location", "data", "organization" }
)
public class NamedEntityRecognizer extends AbstractStanfordCoreNLPWebService
        implements INamedEntityRecognizer {


    public NamedEntityRecognizer() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT,
                PROP_POS_TAG, PROP_LEMMA, PROP_NER);
    }

    @Override
    public String execute(Container container)
            throws StanfordWebServiceException {

        String text = container.getText();
        View view = container.newView();
        view.addContains(Uri.NE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "ner:stanford");
        int id = -1;
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String ner = token.ner();
                if(ner != null && !ner.equalsIgnoreCase("O")) {
                    String type = null;
                    switch (ner.toLowerCase()) {
                        case "person": type = Uri.PERSON;
                            break;
                        case "location": type = Uri.LOCATION;
                            break;
                        case "date": type = Uri.DATE;
                            break;
                        case "organization": type = Uri.ORGANIZATION;
                            break;
                    }
                    if(type != null) {
                        Annotation ann = newAnnotation(view, NE_ID + (++id), type,
                                token.beginPosition(), token.endPosition());
                        ann.addFeature("word", token.value());
                    }
                }
            }
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    @Override
    public String find(String docs) {
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(docs);
        snlp.annotate(annotation);

        StringBuffer sb = new StringBuffer();

        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        for (CoreMap sentence1 : sentences) {
            for (CoreLabel token : sentence1.get(TokensAnnotation.class)) {
                String ne = token.get(NamedEntityTagAnnotation.class);
                if ( ne.equalsIgnoreCase("O") ){
                    sb.append(token.value());
                }
                else {
                    sb.append("<").append(ne).append(">");
                    sb.append(token.value());
                    sb.append("</").append(ne).append(">");
                }
                sb.append(" ");
            }
        }
        // return null;
        return sb.substring(0, sb.length() - 1);
    }

}

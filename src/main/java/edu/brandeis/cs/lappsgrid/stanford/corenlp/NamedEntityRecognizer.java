package edu.brandeis.cs.lappsgrid.stanford.corenlp;

import edu.brandeis.cs.lappsgrid.stanford.StanfordWebServiceException;
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
 * @author Chunqi SHI (shicq@cs.brandeis.edu)
 * @author Keigh Rim (krim@brandeis.edu)
 * @since 2014-01-31
 *
 */
@org.lappsgrid.annotations.ServiceMetadata(
        name = "edu.brandeis.cs.lappsgrid.stanford.corenlp.NamedEntityRecognizer",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "person", "location", "date", "organization" }
)
public class NamedEntityRecognizer extends AbstractStanfordCoreNLPWebService {


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
                        Annotation ann = view.newAnnotation(NE_ID + (++id), type,
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

}

package edu.brandeis.lapps.stanford.corenlp;

import edu.brandeis.lapps.stanford.StanfordWebServiceException;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.LifException;
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
        name = "NamedEntityRecognizer",
        requires_format = { "text", "lif" },
        produces_format = { "lif" },
        produces = { "ne" }
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
        View view = null;
        try {
            view = container.newView();
        } catch (LifException ignored) {
            // this never raises as newView() will check for duplicate view-id internally
        }
        Contains con = view.addContains(Uri.NE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "ner:stanford");
        // TODO: 3/8/2018 change the value when one's ready
        con.put("namedEntityCategorySet", "conll2003.eng");
        int id = -1;
        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);
        List<CoreMap> sents = annotation.get(SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            for (CoreLabel token : sent.get(TokensAnnotation.class)) {
                String label = token.ner();
                if(label != null && !label.equalsIgnoreCase("O")) {
                    label = label.toLowerCase();
                    String type = Uri.NE;
                    Annotation ann = new Annotation(NE_ID + (++id), type, label,
                            token.beginPosition(), token.endPosition());
                    ann.addFeature("category", label);
                    ann.addFeature("word", token.value());
                    view.addAnnotation(ann);
                }
            }
        }
        // set discriminator to LIF
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

}

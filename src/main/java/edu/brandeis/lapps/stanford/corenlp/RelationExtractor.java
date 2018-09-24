package edu.brandeis.lapps.stanford.corenlp;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author krim
 * @since 5/14/2018
 */
public class RelationExtractor extends AbstractStanfordCoreNLPWebService {
    private static String TOOL_DESCRIPTION = "This service is a wrapper around Stanford CoreNLP 3.9.1 providing a coreference resolution service" +
            "\nInternally it uses CoreNLP default \"token\", \"ssplit\", \"pos\", \"lemma\",\"depparse\", \"natlog\", \"relation\" annotators.";

    public RelationExtractor() {
        this.init(PROP_TOKENIZE, PROP_SENTENCE_SPLIT, PROP_POS_TAG, PROP_LEMMA,
                PROP_DEPPARSE, PROP_NATURALLOGIC, PROP_REALTION);
    }

    @Override
    public String execute(Container container) {

        String text = container.getText();

        View view = null;
        view = container.newView();

        view.addContains(Discriminators.Uri.GENERIC_RELATION,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "relation:stanford");

        view.addContains(Discriminators.Uri.MARKABLE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "markable:stanford");

        edu.stanford.nlp.pipeline.Annotation annotation
                = new edu.stanford.nlp.pipeline.Annotation(text);
        snlp.annotate(annotation);

        int rid = 0;
        int mid = 0;
        int sid = 0;
        List<CoreMap> sents = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sent : sents) {
            for (RelationTriple triple : sent.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class)) {
                String predicateId = String.format("%s%s_%s", MENTION_ID, sid, mid++);
                String arg1Id = String.format("%s%s_%s", MENTION_ID, sid, mid++);
                String arg2Id = String.format("%s%s_%s", MENTION_ID, sid, mid++);

                Annotation pred = new Annotation(coreLabelsToRegion(triple.relation, predicateId,
                        Discriminators.Uri.MARKABLE, null));
                pred.addFeature("text",
                        text.substring(Math.toIntExact(pred.getStart()), Math.toIntExact(pred.getEnd())));
                pred.addFeature("type", "predicate");
                Annotation arg1 = new Annotation(coreLabelsToRegion(triple.subject, arg1Id,
                        Discriminators.Uri.MARKABLE, text));
                arg1.addFeature("text",
                        text.substring(Math.toIntExact(arg1.getStart()), Math.toIntExact(arg1.getEnd())));
                arg1.addFeature("type", "argument");
                Annotation arg2 = new Annotation(coreLabelsToRegion(triple.object, arg2Id,
                        Discriminators.Uri.MARKABLE, text));
                arg2.addFeature("text",
                        text.substring(Math.toIntExact(arg2.getStart()), Math.toIntExact(arg2.getEnd())));
                arg2.addFeature("type", "argument");
                view.addAnnotation(pred);
                view.addAnnotation(arg1);
                view.addAnnotation(arg2);
                Annotation relationAnn = view.newAnnotation(
                        String.format("%s%s", REL_ID, rid++),
                        Discriminators.Uri.GENERIC_RELATION);
                relationAnn.addFeature(Features.GenericRelation.ARGUMENTS, Arrays.toString(new String[]{arg1Id, arg2Id}));
                relationAnn.addFeature(Features.GenericRelation.RELATION, predicateId);
                relationAnn.addFeature(Features.GenericRelation.LABEL, triple.relationLemmaGloss());
                relationAnn.addFeature("text",
                        String.format("%s(%s, %s)", triple.relationLemmaGloss(), triple.subjectLemmaGloss(), triple.objectLemmaGloss()
                ));
            }
            mid = 0;
            sid++;
        }

        Data<Container> data = new Data<>(Discriminators.Uri.LIF, container);
        return Serializer.toJson(data);
    }
    
    private Annotation coreLabelsToRegion(List<CoreLabel> cLabels, String annId, String atType, String text) {
        int begin = cLabels.get(0).beginPosition();
        int end = cLabels.get(cLabels.size()-1).endPosition();
        Annotation ann = new Annotation(annId, atType, begin, end);
        return ann;

    }

    @Override
    String loadMetadata() {
        ServiceMetadata metadata = this.setCommonMetadata();
        metadata.setDescription(TOOL_DESCRIPTION);
        metadata.getProduces().addAnnotations(Discriminators.Uri.GENERIC_RELATION, Discriminators.Uri.MARKABLE);

        return new Data<>(Discriminators.Uri.META, metadata).asPrettyJson();
    }

}

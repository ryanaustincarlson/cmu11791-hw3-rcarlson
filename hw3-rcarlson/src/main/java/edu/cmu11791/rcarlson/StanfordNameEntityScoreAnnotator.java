package edu.cmu11791.rcarlson;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ne.type.NamedEntityMention;

import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;


/**
 * Annotations based on Stanford Core NLP tools
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class StanfordNameEntityScoreAnnotator extends AbstractScoreAnnotator {

  private static final String PERSON_TYPE = "PERSON";

  private boolean mentionIsContained(Annotation annotation, NamedEntityMention mention) {
    return annotation.getBegin() <= mention.getBegin() && annotation.getEnd() >= mention.getEnd();
  }
  
  private int getOverlap(List<String> first, List<String> second) {
    int count = 0;
    for (String firstString : first) {
      for (String secondString : second) {
        if (firstString.equals(secondString)) {
          count++;
        }
      }
    }
    return count;
  }

  @Override
  protected void assignScore(JCas jcas, Question question, AnswerScore answerScore) {
    // this is a slow and slightly silly way to do this, but it's much less complicated
    // than re-implementing all the stuff that comes before assignScore()
    List<String> questionEntities = new ArrayList<String>();
    List<String> answerEntities = new ArrayList<String>();

    JFSIndexRepository repo = jcas.getJFSIndexRepository();
    FSIterator<TOP> mentionIter = repo.getAllIndexedFS(NamedEntityMention.type);

    while (mentionIter.hasNext()) {
      NamedEntityMention mention = (NamedEntityMention) mentionIter.next();
      if (mention.getMentionType() != null && mention.getMentionType().equals(PERSON_TYPE)) {
        if (mentionIsContained(question, mention)) {
          questionEntities.add(mention.getCoveredText());
        }
        if (mentionIsContained(answerScore, mention)) {
          answerEntities.add(mention.getCoveredText());
        }
      }
    }

    answerScore.setScore(getOverlap(questionEntities, answerEntities));
    answerScore.setConfidence(0.5);
    answerScore.setCasProcessorId(StanfordNameEntityScoreAnnotator.class.getSimpleName());
  }
}

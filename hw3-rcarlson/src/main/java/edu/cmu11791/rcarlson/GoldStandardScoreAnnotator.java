package edu.cmu11791.rcarlson;

import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;

/**
 * Perfect score annotator. Use Answer.isCorrect() to assign a score. Useful for testing.
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class GoldStandardScoreAnnotator extends AbstractScoreAnnotator {

  @Override
  protected void assignScore(JCas jcas, Question question, AnswerScore answerScore) {
    answerScore.setScore(answerScore.getAnswer().getIsCorrect() ? 1 : 0);
    answerScore.setConfidence(1);
    answerScore.setCasProcessorId(GoldStandardScoreAnnotator.class.getSimpleName());
  }
}

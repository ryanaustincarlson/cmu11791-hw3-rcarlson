package edu.cmu11791.rcarlson;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;

/**
 * For each answer, set the score to be the number of words (tokens) that are shared between the question and answer. For example
 * 
 * Q: Booth loves Lincoln?
 * A: Booth shot Lincoln.
 * 
 * would have score=2 ("Booth" and "Lincoln")
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class TokenOverlapScoreAnnotator extends AbstractScoreAnnotator {

  @Override
  protected void assignScore(JCas jcas, Question question, AnswerScore answerScore) {
    FSArray questionTokens = ((NGram) question.getNgrams().get(0)).getElements(); // unigrams
    Map<String,Integer> questionTokenMap = new HashMap<String,Integer>();
    for (int questionIndex = 0; questionIndex < questionTokens.size(); questionIndex++) {
      Annotation annotation = (Annotation) questionTokens.get(questionIndex);
      String span = annotation.getCoveredText();
      if (!questionTokenMap.containsKey(span)) {
        questionTokenMap.put(span, 0);
      }
      questionTokenMap.put(span, questionTokenMap.get(span)+1);
    }
    
    FSArray answerTokens = ((NGram) answerScore.getAnswer().getNgrams().get(0)).getElements();
    
    int overlapCount = 0;
    
    int numAnswerTokens = answerTokens.size();
    for (int answerIndex = 0; answerIndex < numAnswerTokens; answerIndex++) {
      Annotation annotation = (Annotation) answerTokens.get(answerIndex);
      String text = annotation.getCoveredText();
      if (questionTokenMap.containsKey(text)) {
        overlapCount += questionTokenMap.get(text);
      }
    }
    
    answerScore.setScore(overlapCount);
    answerScore.setConfidence(0.5);
    answerScore.setCasProcessorId(TokenOverlapScoreAnnotator.class.getSimpleName());
  }
}

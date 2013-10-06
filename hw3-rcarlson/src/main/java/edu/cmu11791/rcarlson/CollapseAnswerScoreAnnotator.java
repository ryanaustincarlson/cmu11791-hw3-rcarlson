package edu.cmu11791.rcarlson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;

public class CollapseAnswerScoreAnnotator extends JCasAnnotator_ImplBase {

  private static final String[] VALID_ANNOTATORS = {
      CosineSimilarityScoreAnnotator.class.getSimpleName(), "StanfordNameEntityScoreAnnotator" };

  private boolean shouldConsiderAnswerScore(AnswerScore answerScore) {
    if (answerScore.getCasProcessorId() == null) {
      return false;
    }
    for (String valid_annotator : VALID_ANNOTATORS) {
      if (answerScore.getCasProcessorId().equals(valid_annotator)) {
        return true;
      }
    }
    return false;
  }

  private String getKey(AnswerScore as) {
    return as.getBegin() + "_" + as.getEnd();
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    Iterator<Annotation> answerScoreIter = jcas.getAnnotationIndex(AnswerScore.type).iterator();
    Map<String, List<AnswerScore>> mapping = new HashMap<String, List<AnswerScore>>();

    while (answerScoreIter.hasNext()) {
      AnswerScore answerScore = (AnswerScore) answerScoreIter.next();
      String key = getKey(answerScore);
      if (!mapping.containsKey(key)) {
        mapping.put(key, new ArrayList<AnswerScore>());
      }
      List<AnswerScore> scores = mapping.get(key);
      scores.add(answerScore);
    }

    Set<String> keys = mapping.keySet();
    for (String key : keys) {
      List<AnswerScore> scores = mapping.get(key);
      AnswerScore firstScore = scores.get(0);

      AnswerScore collapsedScore = new AnswerScore(jcas);
      collapsedScore.setAnswer(firstScore.getAnswer());
      collapsedScore.setBegin(firstScore.getBegin());
      collapsedScore.setEnd(firstScore.getEnd());
      collapsedScore.setCasProcessorId(CollapseAnswerScoreAnnotator.class.getSimpleName());
      collapsedScore.setConfidence(0.5);
      double scoreValue = 0;
      for (AnswerScore score : scores) {
        if (shouldConsiderAnswerScore(score)) {
          scoreValue += score.getScore();
        }
      }
      collapsedScore.setScore(scoreValue);
      collapsedScore.addToIndexes();
    }
  }

}

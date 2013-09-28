package edu.cmu11791.rcarlson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.Evaluation;
import edu.cmu.deiis.types.AnswerScore;

/**
 * Sort {@link AnswerScore} objects by score, then calculate precision at *n*, where *n* is the true
 * number of correct answers.
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class EvaluationAnnotator extends JCasAnnotator_ImplBase {

  private boolean shouldConsiderAnswerScore(AnswerScore answerScore) {
    return answerScore.getCasProcessorId().equals(
            CosineSimilarityScoreAnnotator.class.getSimpleName());
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    List<AnswerScore> answerScores = new ArrayList<AnswerScore>();
    int numCorrectAnswers = sortAnswerScores(jcas, answerScores);

    int numCorrect = 0;
    for (int i = 0; i < numCorrectAnswers; i++) {
      AnswerScore answerScore = answerScores.get(i);
      boolean isCorrect = answerScore.getAnswer().getIsCorrect();
      if (isCorrect) {
        numCorrect++;
      }
    }
    FSArray answerScoresArray = new FSArray(jcas, answerScores.size());
    answerScoresArray.copyFromArray(answerScores.toArray(new AnswerScore[answerScores.size()]), 0,
            0, answerScores.size());
    Evaluation evaluation = new Evaluation(jcas);
    evaluation.setAnswers(answerScoresArray);
    evaluation.setPrecisionAtN(1. * numCorrect / numCorrectAnswers);
    evaluation.setBegin(0);
    evaluation.setEnd(jcas.getDocumentText().length());
    evaluation.addToIndexes();
  }

  private int sortAnswerScores(JCas jcas, List<AnswerScore> answerScores) {
    Iterator<Annotation> answerScoreIter = jcas.getAnnotationIndex(AnswerScore.type).iterator();
    int numCorrectAnswers = 0;
    while (answerScoreIter.hasNext()) {
      AnswerScore answerScore = (AnswerScore) answerScoreIter.next();
      if (!shouldConsiderAnswerScore(answerScore)) {
        continue;
      }
      
      answerScores.add(answerScore);
      numCorrectAnswers += answerScore.getAnswer().getIsCorrect() ? 1 : 0;
    }
    Collections.sort(answerScores, new AnswerComparator());
    return numCorrectAnswers;
  }

  private static class AnswerComparator implements Comparator<AnswerScore> {
    public int compare(AnswerScore a1, AnswerScore a2) {
      return a1.getScore() < a2.getScore() ? 1 : -1;
    }
  }
}

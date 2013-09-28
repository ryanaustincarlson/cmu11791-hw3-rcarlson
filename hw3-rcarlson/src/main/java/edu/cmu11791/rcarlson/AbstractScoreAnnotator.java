package edu.cmu11791.rcarlson;

import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;

/**
 * A useful abstraction if a score annotator only needs to know about the (question, answer) pair.
 * 
 * @author Ryan Carlson (rcarlson)
 */
public abstract class AbstractScoreAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    Question question = getQuestion(jcas);

    Iterator<Annotation> answerIter = jcas.getAnnotationIndex(Answer.type).iterator();
    while (answerIter.hasNext()) {
      Answer answer = (Answer) answerIter.next();
      AnswerScore answerScore = new AnswerScore(jcas);
      answerScore.setAnswer(answer);
      answerScore.setBegin(answer.getBegin());
      answerScore.setEnd(answer.getEnd());

      assignScore(jcas, question, answerScore);

      answerScore.addToIndexes();
    }
  }

  public static Question getQuestion(JCas jcas) {
    Iterator<Annotation> questionIter = jcas.getAnnotationIndex(Question.type).iterator();
    Question question = null;
    // assume we have 1 question
    while (questionIter.hasNext()) {
      question = (Question) questionIter.next();
    }
    return question;
  }

  protected abstract void assignScore(JCas jcas, Question question, AnswerScore answerScore);

}

package edu.cmu11791.rcarlson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

/**
 * Parse the document using regular expressions into a Question and multiple Answers.
 * 
 * @author Ryan Carlson (rcarlson)
 */
public class TestElementAnnotator extends JCasAnnotator_ImplBase {

  private Pattern questionPattern = Pattern.compile("Q .+\\?");

  private Pattern answerPattern = Pattern.compile("A (0|1) .+\\.");

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    // get document text
    String docText = jcas.getDocumentText();
    Matcher matcher = questionPattern.matcher(docText);
    while (matcher.find()) {
      Question question = new Question(jcas);
      question.setConfidence(1);
      question.setBegin(matcher.start() + 2); // ignore the "Q "
      question.setEnd(matcher.end());
      question.setCasProcessorId(TestElementAnnotator.class.getSimpleName());
      question.addToIndexes();
    }

    matcher.usePattern(answerPattern);
    while (matcher.find()) {
      Answer answer = new Answer(jcas);
      answer.setConfidence(1);
      answer.setBegin(matcher.start() + 4); // ignore the "A (0|1) "
      answer.setEnd(matcher.end());
      answer.setIsCorrect(matcher.group(1).equals("1"));
      answer.setCasProcessorId(TestElementAnnotator.class.getSimpleName());
      answer.addToIndexes();
    }
  }
}

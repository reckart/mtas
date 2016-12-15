package mtas.parser.cql.util;

import java.util.ArrayList;
import java.util.List;

import mtas.parser.cql.ParseException;
import mtas.search.spans.MtasSpanOrQuery;
import mtas.search.spans.MtasSpanRecurrenceQuery;
import mtas.search.spans.MtasSpanSequenceItem;
import mtas.search.spans.MtasSpanSequenceQuery;
import mtas.search.spans.util.MtasSpanQuery;


/**
 * The Class MtasCQLParserSentenceCondition.
 */
public class MtasCQLParserSentenceCondition {

  // parent list: multiple items for OR
  /** The sequence list. */
  // child list: sequence
  private List<List<MtasCQLParserSentenceCondition>> sequenceList;

  /** The sentence sequence. */
  private List<MtasCQLParserSentenceCondition> sentenceSequence = null;

  /** The sentence current. */
  private MtasCQLParserSentenceCondition sentenceCurrent = null;

  /** The basic sentence. */
  private MtasCQLParserBasicSentenceCondition basicSentence = null;

  /** The maximum occurence. */
  private int minimumOccurence, maximumOccurence;

  /** The optional parts. */
  private boolean basic, simplified, optional, optionalParts;
  
  private MtasSpanQuery ignore;
  
  private Integer maximumIgnoreLength;

  /**
   * Instantiates a new mtas cql parser sentence condition.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public MtasCQLParserSentenceCondition(MtasCQLParserBasicSentenceCondition s, MtasSpanQuery ignore, Integer maximumIgnoreLength)
      throws ParseException {
    sequenceList = new ArrayList<List<MtasCQLParserSentenceCondition>>();
    basicSentence = s;    
    minimumOccurence = 1;
    maximumOccurence = 1;
    simplified = false;
    basic = true;
    optional = false;
    this.ignore = ignore;
    this.maximumIgnoreLength = maximumIgnoreLength;
  }

  /**
   * Instantiates a new mtas cql parser sentence condition.
   *
   * @param sp
   *          the sp
   * @throws ParseException
   *           the parse exception
   */
  public MtasCQLParserSentenceCondition(MtasCQLParserSentenceCondition sp, MtasSpanQuery ignore, Integer maximumIgnoreLength)
      throws ParseException {
    sequenceList = new ArrayList<List<MtasCQLParserSentenceCondition>>();
    addSentenceToEndLatestSequence(sp);
    minimumOccurence = 1;
    maximumOccurence = 1;
    simplified = false;
    basic = false;
    optional = false;
    this.ignore = ignore;
    this.maximumIgnoreLength = maximumIgnoreLength;
  }

  /**
   * Adds the basic sentence to end latest sequence.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public void addBasicSentenceToEndLatestSequence(
      MtasCQLParserBasicSentenceCondition s) throws ParseException {
    if (!simplified) {
      if (isBasic()) {
        if (basicSentence == null) {
          basicSentence = s;
        } else {
          basicSentence.addBasicSentence(s);
        }
      } else {
        sentenceCurrent = new MtasCQLParserSentenceCondition(s, ignore, maximumIgnoreLength);
        sentenceSequence.add(sentenceCurrent);
      }
    } else {
      throw new ParseException("already simplified");
    }
  }

  /**
   * Adds the basic sentence as option.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public void addBasicSentenceAsOption(MtasCQLParserBasicSentenceCondition s)
      throws ParseException {
    if (!simplified) {
      if (isBasic()) {
        if (basicSentence == null) {
          basicSentence = s;
        } else {
          // add previous basic sentence as first option
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = new MtasCQLParserSentenceCondition(basicSentence, ignore, maximumIgnoreLength);
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          basicSentence = null;
          // create new option for current basic sentence
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = new MtasCQLParserSentenceCondition(s, ignore, maximumIgnoreLength);
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // not basic anymore
          basic = false;
        }
      } else {
        sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
        sentenceCurrent = new MtasCQLParserSentenceCondition(s, ignore, maximumIgnoreLength);
        sentenceSequence.add(sentenceCurrent);
        sequenceList.add(sentenceSequence);
      }
    } else {
      throw new ParseException("already simplified");
    }
  }

  /**
   * Adds the sentence to start first sequence.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public void addSentenceToStartFirstSequence(MtasCQLParserSentenceCondition s)
      throws ParseException {
    if (!simplified) {
      if (isBasic()) {
        if (basicSentence == null) {
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // not basic anymore
          basic = false;
        } else {
          // add sentence as first item in new sequence
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          // add sentence to first option
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          // add basic sentence as second item
          sentenceCurrent = new MtasCQLParserSentenceCondition(basicSentence, ignore, maximumIgnoreLength);
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          basicSentence = null;
          // not simple anymore
          basic = false;
        }
      } else {
        sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
        sentenceSequence.add(s);
        sentenceSequence.addAll(sequenceList.get(0));
        sequenceList.set(0, sentenceSequence);
        sentenceSequence = sequenceList.get((sequenceList.size() - 1));
        sentenceCurrent = sentenceSequence.get((sentenceSequence.size() - 1));
      }
    } else {
      throw new ParseException("already simplified");
    }
  }

  /**
   * Adds the sentence to end latest sequence.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public void addSentenceToEndLatestSequence(MtasCQLParserSentenceCondition s)
      throws ParseException {
    if (!simplified) {
      if (isBasic()) {
        if (basicSentence == null) {
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // not simple anymore
          basic = false;
        } else {
          // add previous basic sentence as first option
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = new MtasCQLParserSentenceCondition(basicSentence, ignore, maximumIgnoreLength);
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          basicSentence = null;
          // add sentence to first option
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          // not simple anymore
          basic = false;
        }
      } else {
        sentenceCurrent = s;
        if (sentenceSequence == null) {
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sequenceList.add(sentenceSequence);
        }
        sentenceSequence.add(sentenceCurrent);
      }
    } else {
      throw new ParseException("already simplified");
    }
  }

  /**
   * Adds the sentence as first option.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public void addSentenceAsFirstOption(MtasCQLParserSentenceCondition s)
      throws ParseException {
    if (!simplified) {
      if (isBasic()) {
        if (basicSentence == null) {
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // not simple anymore
          basic = false;
        } else {
          // add sentence as first option
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // add previous basic sentence as new option
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = new MtasCQLParserSentenceCondition(basicSentence, ignore, maximumIgnoreLength);
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          basicSentence = null;
          // not simple anymore
          basic = false;
        }
      } else {
        sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
        sentenceCurrent = s;
        sentenceSequence.add(sentenceCurrent);
        List<List<MtasCQLParserSentenceCondition>> newsequenceList = new ArrayList<List<MtasCQLParserSentenceCondition>>();
        newsequenceList.add(sentenceSequence);
        newsequenceList.addAll(sequenceList);
        sequenceList = newsequenceList;
      }
    } else {
      throw new ParseException("already simplified");
    }
  }

  /**
   * Adds the sentence as option.
   *
   * @param s
   *          the s
   * @throws ParseException
   *           the parse exception
   */
  public void addSentenceAsOption(MtasCQLParserSentenceCondition s)
      throws ParseException {
    if (!simplified) {
      if (isBasic()) {
        if (basicSentence == null) {
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // not simple anymore
          basic = false;
        } else {
          // add previous basic sentence as first option
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = new MtasCQLParserSentenceCondition(basicSentence, ignore, maximumIgnoreLength);
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          basicSentence = null;
          // add sentence as new option
          sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
          sentenceCurrent = s;
          sentenceSequence.add(sentenceCurrent);
          sequenceList.add(sentenceSequence);
          // not simple anymore
          basic = false;
        }
      } else {
        sentenceSequence = new ArrayList<MtasCQLParserSentenceCondition>();
        sentenceCurrent = s;
        sentenceSequence.add(sentenceCurrent);
        sequenceList.add(sentenceSequence);
      }
    } else {
      throw new ParseException("already simplified");
    }
  }

  /**
   * Checks if is basic.
   *
   * @return true, if is basic
   */
  public boolean isBasic() {
    return basic;
  }

  /**
   * Checks if is single.
   *
   * @return true, if is single
   */
  public boolean isSingle() {
    return basic ? true : ((sequenceList.size() > 1) ? false : true);
  }

  /**
   * Simplify.
   *
   * @throws ParseException
   *           the parse exception
   */
  public void simplify() throws ParseException {
    if (!simplified) {
      if (!isBasic()) {
        for (List<MtasCQLParserSentenceCondition> sequence : sequenceList) {
          simplifySequence(sequence);
        }
      }
      // flatten
      if (sequenceList.size() > 1) {
        List<List<MtasCQLParserSentenceCondition>> newSequenceList = new ArrayList<List<MtasCQLParserSentenceCondition>>();
        for (List<MtasCQLParserSentenceCondition> sequence : sequenceList) {
          if (sequence.size() == 1) {
            MtasCQLParserSentenceCondition subSentence = sequence.get(0);
            if (subSentence.isBasic()) {
              newSequenceList.add(sequence);
            } else {
              newSequenceList.addAll(subSentence.getsequenceList());
            }
          }
        }
        sequenceList = newSequenceList;
      }
      simplified = true;
    }
  }

  /**
   * Simplify sequence.
   *
   * @param sequence
   *          the sequence
   * @throws ParseException
   *           the parse exception
   */
  private void simplifySequence(List<MtasCQLParserSentenceCondition> sequence)
      throws ParseException {
    List<MtasCQLParserSentenceCondition> newSequence = new ArrayList<MtasCQLParserSentenceCondition>();
    MtasCQLParserSentenceCondition lastSentence = null;
    for (MtasCQLParserSentenceCondition sentence : sequence) {
      sentence.simplify();
      if (lastSentence == null) {
        lastSentence = sentence;
      } else if (lastSentence.isBasic() && sentence.isBasic()) {
        // if no recurrence or equal queries
        // opt1 optPart1 opt2 optPart2 merge opt optPart
        // ..-.....-.....-........-.....+.....-.....-
        // ..+.....-.....-........-.....-............
        // ..+.....+.....-........-.....+.....-.....-
        // ..-.....-.....+........-.....-............
        // ..+.....-.....+........-.....-............
        // ..+.....+.....+........-.....+.....+.....-
        // ..-.....-.....+........+.....+.....-.....-
        // ..+.....-.....+........+.....+.....+.....-
        // ..+.....+.....+........+.....+.....+.....+
        if ((((lastSentence.getMaximumOccurence() == 1)
            && (sentence.getMaximumOccurence() == 1))
            || lastSentence.getQuery().equals(sentence.getQuery())
                && ((!lastSentence.isOptional() && !sentence.isOptional()))
            || lastSentence.hasOptionalParts()
            || sentence.hasOptionalParts())) {
          // create new basic sentence
          MtasCQLParserBasicSentenceCondition newBasicSentence = new MtasCQLParserBasicSentenceCondition(ignore, maximumIgnoreLength);
          newBasicSentence.addBasicSentence(lastSentence.basicSentence);
          newBasicSentence.addBasicSentence(sentence.basicSentence);
          // make optional
          if (lastSentence.isOptional() && sentence.isOptional()) {
            newBasicSentence.setOptional(true);
          }
          lastSentence = new MtasCQLParserSentenceCondition(newBasicSentence, ignore, maximumIgnoreLength);
          lastSentence.simplify();
        } else {
          newSequence.add(lastSentence);
          lastSentence = sentence;
        }
      } else if (lastSentence.isBasic()) {
        if (sentence.isSingle()
            && (!sentence.isOptional() || sentence.hasOptionalParts())
            && ((sentence.getMaximumOccurence() == 1)
                && (lastSentence.getMaximumOccurence() == 1))) {
          for (MtasCQLParserSentenceCondition subSentence : sentence
              .getsequenceList().get(0)) {
            newSequence.add(lastSentence);
            lastSentence = subSentence;
          }
        } else {
          newSequence.add(lastSentence);
          lastSentence = sentence;
        }
      } else if (sentence.isBasic()) {
        if (lastSentence.isSingle()
            && (!lastSentence.isOptional() || lastSentence.hasOptionalParts())
            && ((sentence.getMaximumOccurence() == 1)
                && (lastSentence.getMaximumOccurence() == 1))) {
          lastSentence
              .addBasicSentenceToEndLatestSequence(sentence.getBasicSentence());
        } else {
          newSequence.add(lastSentence);
          lastSentence = sentence;
        }
      } else {
        newSequence.add(lastSentence);
        lastSentence = sentence;
      }
    }
    if (lastSentence != null) {
      newSequence.add(lastSentence);
    }
    sequence.clear();
    sequence.addAll(newSequence);
  }

  /**
   * Gets the sequence list.
   *
   * @return the sequence list
   */
  public List<List<MtasCQLParserSentenceCondition>> getsequenceList() {
    return sequenceList;
  }

  /**
   * Gets the basic sentence.
   *
   * @return the basic sentence
   */
  public MtasCQLParserBasicSentenceCondition getBasicSentence() {
    return basicSentence;
  }

  /**
   * Gets the minimum occurence.
   *
   * @return the minimum occurence
   */
  public int getMinimumOccurence() {
    return minimumOccurence;
  }

  /**
   * Gets the maximum occurence.
   *
   * @return the maximum occurence
   */
  public int getMaximumOccurence() {
    return maximumOccurence;
  }

  /**
   * Sets the occurence.
   *
   * @param min
   *          the min
   * @param max
   *          the max
   * @throws ParseException
   *           the parse exception
   */
  public void setOccurence(int min, int max) throws ParseException {
    if ((min < 0) || (min > max) || (max < 1)) {
      throw new ParseException("Illegal number {" + min + "," + max + "}");
    }
    if (min == 0) {
      optional = true;
    }
    minimumOccurence = Math.max(1, min);
    maximumOccurence = max;
  }

  /**
   * Checks if is optional.
   *
   * @return true, if is optional
   */
  public boolean isOptional() {
    return optional;
  }

  /**
   * Checks for optional parts.
   *
   * @return true, if successful
   * @throws ParseException
   *           the parse exception
   */
  public boolean hasOptionalParts() throws ParseException {
    if (simplified) {
      return optionalParts;
    } else {
      throw new ParseException("can't be called when not simplified");
    }
  }

  /**
   * Sets the optional.
   *
   * @param status
   *          the new optional
   */
  public void setOptional(boolean status) {
    optional = status;
  }

  /**
   * Creates the query.
   *
   * @param sentenceSequence
   *          the sentence sequence
   * @return the span query
   * @throws ParseException
   *           the parse exception
   */
  private MtasSpanQuery createQuery(
      List<MtasCQLParserSentenceCondition> sentenceSequence)
      throws ParseException {
    if (sentenceSequence.size() == 1) {
      if (maximumOccurence > 1) {
        return new MtasSpanRecurrenceQuery(sentenceSequence.get(0).getQuery(),
            minimumOccurence, maximumOccurence, ignore, maximumIgnoreLength);
      } else {
        return sentenceSequence.get(0).getQuery();
      }
    } else {
      List<MtasSpanSequenceItem> clauses = new ArrayList<MtasSpanSequenceItem>();
      for (MtasCQLParserSentenceCondition sentence : sentenceSequence) {
        clauses.add(
            new MtasSpanSequenceItem(sentence.getQuery(), sentence.optional));
      }
      if (maximumOccurence > 1) {
        return new MtasSpanRecurrenceQuery(new MtasSpanSequenceQuery(clauses, ignore, maximumIgnoreLength),
            minimumOccurence, maximumOccurence, ignore, maximumIgnoreLength);
      } else {
        return new MtasSpanSequenceQuery(clauses, ignore, maximumIgnoreLength);
      }
    }
  }

  /**
   * Gets the query.
   *
   * @return the query
   * @throws ParseException
   *           the parse exception
   */
  public MtasSpanQuery getQuery() throws ParseException {
    simplify();
    if (isBasic()) {
      MtasSpanQuery query;
      if (basicSentence == null) {
        throw new ParseException("no condition");
      } else if (basicSentence.isOptional()) {
        List<MtasSpanSequenceItem> clauses = new ArrayList<MtasSpanSequenceItem>();
        clauses.add(new MtasSpanSequenceItem(basicSentence.getQuery(),
            basicSentence.isOptional()));
        query = new MtasSpanSequenceQuery(clauses, ignore, maximumIgnoreLength);
        if (maximumOccurence > 1) {
          query = new MtasSpanRecurrenceQuery(query, minimumOccurence,
              maximumOccurence, ignore, maximumIgnoreLength);
        }
      } else {
        query = basicSentence.getQuery();
        if (maximumOccurence > 1) {
          query = new MtasSpanRecurrenceQuery(query, minimumOccurence,
              maximumOccurence, ignore, maximumIgnoreLength);
        }
      }
      return query;
    } else if (sequenceList.isEmpty()) {
      throw new ParseException("no condition");
    } else if (isSingle()) {
      return createQuery(sequenceList.get(0));
    } else {
      List<MtasSpanQuery> clauses = new ArrayList<MtasSpanQuery>();
      for (List<MtasCQLParserSentenceCondition> sentenceSequence : sequenceList) {
        clauses.add(createQuery(sentenceSequence));
      }
      return new MtasSpanOrQuery(
          clauses.toArray(new MtasSpanQuery[clauses.size()]));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return toString("", "");
  }

  /**
   * To string.
   *
   * @param firstIndent
   *          the first indent
   * @param indent
   *          the indent
   * @return the string
   */
  public String toString(String firstIndent, String indent) {
    String text = "";
    if (isBasic()) {
      try {
        text += firstIndent + "BASIC SENTENCE" + (optional ? " OPTIONAL" : "")
            + ": " + basicSentence.getQuery()
            + (basicSentence.isOptional() ? " OPTIONAL" : "") + "\n";
      } catch (ParseException e) {
        text += firstIndent + "BASIC SENTENCE" + (optional ? " OPTIONAL" : "")
            + ": " + e.getMessage() + "\n";
      }
    } else {
      text += firstIndent + "SENTENCE" + (optional ? " OPTIONAL" : "") + "\n";
      if (simplified) {
        try {
          text += indent + "- Query: "
              + getQuery().toString(getQuery().getField());
        } catch (ParseException e) {
          text += indent + "- Query: " + e.getMessage();
        }
        text += "\n";
      } else {
        for (List<MtasCQLParserSentenceCondition> sentenceSequence : sequenceList) {
          text += indent + "- Sequence :\n";
          for (MtasCQLParserSentenceCondition sentence : sentenceSequence) {
            text += sentence.toString(indent + "  - ", indent + "    ");
          }
        }
        text += "\n";
      }
    }
    return text;
  }

}

package com.moderation.model;

/**
 * Represents a single entry in the banned-word dictionary.
 * Each word has a severity level that drives the moderation action.
 *
 * Module Owner : Person 1 - Core Moderation & Severity Engine
 * Branch       : feature-severity-engine
 */
public class BannedWord {

    private final String   word;
    private final Severity severity;

    public BannedWord(String word, Severity severity) {
        this.word     = word.toLowerCase().trim();
        this.severity = severity;
    }

    /** The normalised (lowercase) form of the banned word. */
    public String   getWord()     { return word; }

    /** The severity classification of this word. */
    public Severity getSeverity() { return severity; }

    @Override
    public String toString() {
        return String.format("BannedWord{word='%s', severity=%s}", word, severity);
    }
}

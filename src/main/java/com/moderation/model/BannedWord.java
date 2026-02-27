package com.moderation.model;

public class BannedWord {

    private final String word;
    private final Severity severity;

    public BannedWord(String word, Severity severity) {
        this.word = word.toLowerCase();
        this.severity = severity;
    }

    public String getWord()       { return word; }
    public Severity getSeverity() { return severity; }
}

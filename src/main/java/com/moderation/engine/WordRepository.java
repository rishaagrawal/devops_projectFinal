package com.moderation.engine;

import com.moderation.model.BannedWord;
import com.moderation.model.Severity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * MODULE : Word Repository
 * PERSON : Person 1 - Core Moderation & Severity Engine
 * BRANCH : feature-severity-engine
 * ============================================================
 *
 * Stores and manages the banned-word dictionary.
 * Pre-loaded with default words in three severity tiers;
 * supports runtime addition and removal.
 *
 * Default dictionary:
 * ???????????????????????????????????????????????????????????
 * ? HIGH     ? bomb, terrorist, kill, murder, hack, exploit ?
 * ? MEDIUM   ? drug, violence, weapon, cheat, scam          ?
 * ? LOW      ? idiot, stupid, dumb, loser                   ?
 * ???????????????????????????????????????????????????????????
 */
public class WordRepository {

    private final List<BannedWord> bannedWords = new ArrayList<>();

    public WordRepository() {
        loadDefaults();
    }

    // ------------------------------------------------------------------
    //  Default word list
    // ------------------------------------------------------------------

    private void loadDefaults() {
        // HIGH severity - immediate block
        add("bomb",      Severity.HIGH);
        add("terrorist", Severity.HIGH);
        add("kill",      Severity.HIGH);
        add("murder",    Severity.HIGH);
        add("hack",      Severity.HIGH);
        add("exploit",   Severity.HIGH);

        // MEDIUM severity - flagged for review
        add("drug",     Severity.MEDIUM);
        add("violence", Severity.MEDIUM);
        add("weapon",   Severity.MEDIUM);
        add("cheat",    Severity.MEDIUM);
        add("scam",     Severity.MEDIUM);

        // LOW severity - allowed with warning
        add("idiot",  Severity.LOW);
        add("stupid", Severity.LOW);
        add("dumb",   Severity.LOW);
        add("loser",  Severity.LOW);
    }

    private void add(String word, Severity severity) {
        bannedWords.add(new BannedWord(word, severity));
    }

    // ------------------------------------------------------------------
    //  Public API
    // ------------------------------------------------------------------

    /** Returns an unmodifiable view of all banned words. */
    public List<BannedWord> getBannedWords() {
        return Collections.unmodifiableList(bannedWords);
    }

    /** Adds a new banned word at runtime. Duplicates are allowed (first match wins). */
    public void addWord(BannedWord word) {
        bannedWords.add(word);
    }

    /** Removes a word from the list (case-insensitive). */
    public void removeWord(String word) {
        bannedWords.removeIf(bw -> bw.getWord().equalsIgnoreCase(word.trim()));
    }

    /** Returns all words with the given severity level. */
    public List<BannedWord> getWordsBySeverity(Severity severity) {
        return bannedWords.stream()
                .filter(bw -> bw.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /** Returns the total number of entries in the dictionary. */
    public int size() {
        return bannedWords.size();
    }

    /** Returns true if the given word (exact, normalised) is in the dictionary. */
    public boolean contains(String word) {
        String lower = word.toLowerCase().trim();
        return bannedWords.stream().anyMatch(bw -> bw.getWord().equals(lower));
    }
}

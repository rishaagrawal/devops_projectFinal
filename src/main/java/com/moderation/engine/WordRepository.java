package com.moderation.engine;

import com.moderation.model.BannedWord;
import com.moderation.model.Severity;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and manages the list of banned words with their severity levels.
 * Acts as the rule dictionary for the moderation engine.
 */
public class WordRepository {

    private final List<BannedWord> bannedWords = new ArrayList<>();

    public WordRepository() {
        loadDefaults();
    }

    private void loadDefaults() {
        // HIGH severity
        bannedWords.add(new BannedWord("bomb",       Severity.HIGH));
        bannedWords.add(new BannedWord("terrorist",  Severity.HIGH));
        bannedWords.add(new BannedWord("kill",       Severity.HIGH));
        bannedWords.add(new BannedWord("murder",     Severity.HIGH));
        bannedWords.add(new BannedWord("hack",       Severity.HIGH));
        bannedWords.add(new BannedWord("exploit",    Severity.HIGH));

        // MEDIUM severity
        bannedWords.add(new BannedWord("drug",       Severity.MEDIUM));
        bannedWords.add(new BannedWord("violence",   Severity.MEDIUM));
        bannedWords.add(new BannedWord("weapon",     Severity.MEDIUM));
        bannedWords.add(new BannedWord("cheat",      Severity.MEDIUM));
        bannedWords.add(new BannedWord("scam",       Severity.MEDIUM));

        // LOW severity
        bannedWords.add(new BannedWord("idiot",      Severity.LOW));
        bannedWords.add(new BannedWord("stupid",     Severity.LOW));
        bannedWords.add(new BannedWord("dumb",       Severity.LOW));
        bannedWords.add(new BannedWord("loser",      Severity.LOW));
    }

    public void addWord(BannedWord word) {
        bannedWords.add(word);
    }

    public List<BannedWord> getBannedWords() {
        return bannedWords;
    }

    public void removeWord(String word) {
        bannedWords.removeIf(bw -> bw.getWord().equalsIgnoreCase(word));
    }
}

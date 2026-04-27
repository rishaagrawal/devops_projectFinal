package com.moderation;

import com.moderation.engine.ModerationEngine;
import com.moderation.engine.WordRepository;
import com.moderation.log.AuditLogger;
import com.moderation.model.*;

import java.util.*;

/**
 * Content Moderation Engine - Interactive Menu
 * Clean submenu-based interface. No unicode box characters.
 */
public class Main {

    static final String SEP  = "=".repeat(55);
    static final String LINE = "-".repeat(55);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ModerationEngine engine = new ModerationEngine();

        printBanner();
        String  userId  = promptUserId(scanner);
        Context context = promptContext(scanner);

        boolean running = true;
        while (running) {
            printMainMenu(userId, context);
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    menuModerate(scanner, engine, userId, context);
                    break;
                case "2":
                    menuAudit(scanner, engine.getAuditLogger());
                    break;
                case "3":
                    menuStrikes(scanner, engine);
                    break;
                case "4":
                    menuDictionary(scanner, engine.getWordRepository());
                    break;
                case "5":
                    userId = promptUserId(scanner);
                    break;
                case "6":
                    context = promptContext(scanner);
                    break;
                case "7":
                    menuNormDemo(scanner);
                    break;
                case "8":
                case "exit":
                    engine.getAuditLogger().printAll();
                    engine.getAuditLogger().printSummary();
                    System.out.println("\n  Goodbye!\n");
                    scanner.close();
                    running = false;
                    break;
                default:
                    System.out.println("  Invalid option. Enter 1-8.\n");
            }
        }
    }

    // =========================================================
    //  MAIN MENU
    // =========================================================

    static void printMainMenu(String userId, Context context) {
        System.out.println();
        System.out.println(SEP);
        System.out.println("  CONTENT MODERATION ENGINE");
        System.out.println("  User: " + userId + "   |   Context: " + context);
        System.out.println(LINE);
        System.out.println("  1. Moderate a Message");
        System.out.println("  2. Audit Log");
        System.out.println("  3. Strike Manager");
        System.out.println("  4. Word Dictionary");
        System.out.println("  5. Change User");
        System.out.println("  6. Change Context");
        System.out.println("  7. Normalization Demo");
        System.out.println("  8. Exit");
        System.out.println(SEP);
        System.out.print("  Choice: ");
    }

    // =========================================================
    //  1. MODERATE A MESSAGE
    // =========================================================

    static void menuModerate(Scanner sc, ModerationEngine engine,
                              String userId, Context context) {
        header("MODERATE A MESSAGE");
        System.out.println("  User: " + userId + "   Context: " + context);
        System.out.println();
        System.out.print("  Enter message (or 'back'): ");
        String input = sc.nextLine().trim();
        if (input.equalsIgnoreCase("back") || input.isEmpty()) return;

        ModerationResult r = engine.moderate(userId, input, context);
        System.out.println();
        System.out.println(LINE);
        System.out.println("  RESULT");
        System.out.println(LINE);
        if (r.isClean()) {
            System.out.println("  Status   : CLEAN - No violations");
        } else {
            System.out.println("  Status   : VIOLATION DETECTED");
            System.out.println("  Word     : " + r.getTriggeredWord());
            System.out.println("  Severity : " + r.getSeverity());
        }
        System.out.println("  Action   : " + formatAction(r.getAction()));
        System.out.println("  Reason   : " + r.getReason());
        System.out.println("  Strikes  : " + engine.getStrikeManager().getStrikeCount(userId));
        System.out.println("  Decoded  : " + r.getNormalizedText());
        System.out.println(LINE);
        pause(sc);
    }

    // =========================================================
    //  2. AUDIT LOG SUBMENU
    // =========================================================

    static void menuAudit(Scanner sc, AuditLogger logger) {
        boolean back = false;
        while (!back) {
            header("AUDIT LOG");
            System.out.println("  1. View Full Log");
            System.out.println("  2. Filter by User");
            System.out.println("  3. Filter by Action");
            System.out.println("  4. Filter by Severity");
            System.out.println("  5. Summary Statistics");
            System.out.println("  6. Back to Main Menu");
            System.out.println(LINE);
            System.out.print("  Choice: ");
            switch (sc.nextLine().trim()) {
                case "1":
                    header("FULL AUDIT LOG");
                    logger.printAll();
                    pause(sc);
                    break;
                case "2":
                    System.out.print("  Enter User ID: ");
                    String uid = sc.nextLine().trim();
                    List<AuditLog> ul = logger.getLogsForUser(uid);
                    System.out.println("\n  " + ul.size() + " entries for user '" + uid + "':\n");
                    if (ul.isEmpty()) System.out.println("  No entries found.");
                    else ul.forEach(l -> System.out.println("  " + l));
                    System.out.println();
                    pause(sc);
                    break;
                case "3":
                    header("FILTER BY ACTION");
                    ModerationAction[] actions = ModerationAction.values();
                    for (int i = 0; i < actions.length; i++)
                        System.out.println("  " + (i + 1) + ". " + actions[i]);
                    System.out.print("  Choice: ");
                    try {
                        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                        List<AuditLog> al = logger.getLogsByAction(actions[idx]);
                        System.out.println("\n  " + al.size() + " entries:\n");
                        al.forEach(l -> System.out.println("  " + l));
                    } catch (Exception e) {
                        System.out.println("  Invalid choice.");
                    }
                    System.out.println();
                    pause(sc);
                    break;
                case "4":
                    header("FILTER BY SEVERITY");
                    System.out.println("  1. HIGH   2. MEDIUM   3. LOW");
                    System.out.print("  Choice: ");
                    String sc4 = sc.nextLine().trim();
                    Severity sev = sc4.equals("1") ? Severity.HIGH
                                 : sc4.equals("2") ? Severity.MEDIUM
                                 : sc4.equals("3") ? Severity.LOW : null;
                    if (sev == null) { System.out.println("  Invalid.\n"); break; }
                    List<AuditLog> sl = logger.getLogsBySeverity(sev);
                    System.out.println("\n  " + sl.size() + " entries with severity " + sev + ":\n");
                    if (sl.isEmpty()) System.out.println("  No entries found.");
                    else sl.forEach(l -> System.out.println("  " + l));
                    System.out.println();
                    pause(sc);
                    break;
                case "5":
                    header("AUDIT SUMMARY");
                    logger.printSummary();
                    pause(sc);
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println("  Invalid option.\n");
            }
        }
    }

    // =========================================================
    //  3. STRIKE MANAGER SUBMENU
    // =========================================================

    static void menuStrikes(Scanner sc, ModerationEngine engine) {
        boolean back = false;
        while (!back) {
            header("STRIKE MANAGER");
            System.out.println("  1. View All User Strikes");
            System.out.println("  2. View Strikes for a User");
            System.out.println("  3. Reset a User's Strikes");
            System.out.println("  4. Reset All Strikes");
            System.out.println("  5. Back to Main Menu");
            System.out.println(LINE);
            System.out.print("  Choice: ");
            switch (sc.nextLine().trim()) {
                case "1":
                    header("STRIKE BOARD");
                    Map<String, Integer> all = engine.getStrikeManager().getAllStrikes();
                    if (all.isEmpty()) {
                        System.out.println("  No strikes recorded yet.\n");
                    } else {
                        System.out.printf("  %-20s  %-8s  %s%n", "User", "Strikes", "Status");
                        System.out.println("  " + LINE);
                        all.entrySet().stream()
                           .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                           .forEach(e -> {
                               int s = e.getValue();
                               String st = s == 1 ? "Warning"
                                         : s == 2 ? "Temp Blocked"
                                         : "Permanently Blocked";
                               System.out.printf("  %-20s  %-8d  %s%n", e.getKey(), s, st);
                           });
                        System.out.println();
                    }
                    pause(sc);
                    break;
                case "2":
                    System.out.print("  Enter User ID: ");
                    String u2 = sc.nextLine().trim();
                    int cnt = engine.getStrikeManager().getStrikeCount(u2);
                    String st = cnt == 0 ? "No strikes"
                              : cnt == 1 ? "Warning issued"
                              : cnt == 2 ? "Temporarily blocked"
                              : "Permanently blocked";
                    System.out.println("  User '" + u2 + "': " + cnt + " strike(s) ? " + st + "\n");
                    pause(sc);
                    break;
                case "3":
                    System.out.print("  Enter User ID to reset: ");
                    String u3 = sc.nextLine().trim();
                    engine.getStrikeManager().resetStrikes(u3);
                    System.out.println("  Strikes for '" + u3 + "' reset to 0.\n");
                    pause(sc);
                    break;
                case "4":
                    engine.getStrikeManager().resetAllStrikes();
                    System.out.println("  All strikes have been reset.\n");
                    pause(sc);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("  Invalid option.\n");
            }
        }
    }

    // =========================================================
    //  4. WORD DICTIONARY SUBMENU
    // =========================================================

    static void menuDictionary(Scanner sc, WordRepository repo) {
        boolean back = false;
        while (!back) {
            header("WORD DICTIONARY");
            System.out.println("  1. View All Words by Severity");
            System.out.println("  2. Add a Word");
            System.out.println("  3. Remove a Word");
            System.out.println("  4. Check if Word Exists");
            System.out.println("  5. Back to Main Menu");
            System.out.println(LINE);
            System.out.print("  Choice: ");
            switch (sc.nextLine().trim()) {
                case "1":
                    header("BANNED WORDS");
                    for (Severity s : new Severity[]{Severity.HIGH, Severity.MEDIUM, Severity.LOW}) {
                        System.out.println("\n  [ " + s + " ]");
                        repo.getWordsBySeverity(s)
                            .forEach(w -> System.out.println("    - " + w.getWord()));
                    }
                    System.out.println("\n  Total: " + repo.size() + " words\n");
                    pause(sc);
                    break;
                case "2":
                    System.out.print("  Word to add: ");
                    String nw = sc.nextLine().trim().toLowerCase();
                    System.out.println("  Severity: 1=HIGH  2=MEDIUM  3=LOW");
                    System.out.print("  Choice: ");
                    String ns2 = sc.nextLine().trim();
                    Severity ns = ns2.equals("1") ? Severity.HIGH
                                : ns2.equals("2") ? Severity.MEDIUM : Severity.LOW;
                    repo.addWord(new BannedWord(nw, ns));
                    System.out.println("  Added '" + nw + "' as " + ns + "\n");
                    pause(sc);
                    break;
                case "3":
                    System.out.print("  Word to remove: ");
                    String rw = sc.nextLine().trim();
                    repo.removeWord(rw);
                    System.out.println("  '" + rw + "' removed (if it existed).\n");
                    pause(sc);
                    break;
                case "4":
                    System.out.print("  Word to check: ");
                    String cw = sc.nextLine().trim();
                    System.out.println("  '" + cw + "' is "
                            + (repo.contains(cw) ? "FOUND" : "NOT found")
                            + " in the dictionary.\n");
                    pause(sc);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println("  Invalid option.\n");
            }
        }
    }

    // =========================================================
    //  7. NORMALIZATION DEMO
    // =========================================================

    static void menuNormDemo(Scanner sc) {
        header("NORMALIZATION DEMO");
        System.out.println("  Shows how the engine decodes leet-speak and symbols.");
        System.out.println();
        System.out.print("  Enter text (e.g. k1ll, b@d, $cam): ");
        String raw = sc.nextLine().trim();
        if (raw.isEmpty()) return;

        String norm   = com.moderation.util.TextNormalizer.normalize(raw);
        String simple = com.moderation.util.TextNormalizer.normalizeSimple(raw);

        System.out.println();
        System.out.println(LINE);
        System.out.println("  Original     : " + raw);
        System.out.println("  Full decode  : " + norm);
        System.out.println("  Simple clean : " + simple);
        System.out.println(LINE);
        System.out.println();
        System.out.println("  Substitution table:");
        System.out.println("  @ -> a   0 -> o   1 -> i   $ -> s");
        System.out.println("  3 -> e   4 -> a   5 -> s   7 -> t");
        System.out.println("  hyphens, dots, symbols are removed");
        System.out.println();
        pause(sc);
    }

    // =========================================================
    //  SHARED HELPERS
    // =========================================================

    static void printBanner() {
        System.out.println();
        System.out.println(SEP);
        System.out.println("    CONTENT MODERATION ENGINE  ");

        System.out.println(SEP);
        System.out.println();
    }

    static String promptUserId(Scanner sc) {
        System.out.print("  Enter User ID (default: guest): ");
        String u = sc.nextLine().trim();
        return u.isEmpty() ? "guest" : u;
    }

    static Context promptContext(Scanner sc) {
        System.out.println();
        System.out.println("  Select Context:");
        System.out.println("  1. GENERAL     - standard rules");
        System.out.println("  2. EDUCATIONAL - academic words allowed");
        System.out.println("  3. MEDICAL     - clinical terms allowed");
        System.out.println("  4. GAMING      - cheating always blocked");
        System.out.print("  Choice (default=1): ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "2": System.out.println("  Context set to EDUCATIONAL\n"); return Context.EDUCATIONAL;
            case "3": System.out.println("  Context set to MEDICAL\n");     return Context.MEDICAL;
            case "4": System.out.println("  Context set to GAMING\n");      return Context.GAMING;
            default:  System.out.println("  Context set to GENERAL\n");     return Context.GENERAL;
        }
    }

    static String formatAction(ModerationAction a) {
        if (a == null) return "ALLOW";
        switch (a) {
            case ALLOW:              return "ALLOWED";
            case ALLOW_WITH_WARNING: return "ALLOWED WITH WARNING";
            case FLAG:               return "FLAGGED FOR REVIEW";
            case TEMPORARY_BLOCK:    return "TEMPORARILY BLOCKED";
            case BLOCK:              return "BLOCKED";
            case PERMANENT_BLOCK:    return "PERMANENTLY BLOCKED";
            default:                 return a.toString();
        }
    }

    static void header(String title) {
        System.out.println();
        System.out.println(SEP);
        System.out.println("  " + title);
        System.out.println(LINE);
    }

    static void pause(Scanner sc) {
        System.out.print("  [Press Enter to continue...]");
        sc.nextLine();
    }
}

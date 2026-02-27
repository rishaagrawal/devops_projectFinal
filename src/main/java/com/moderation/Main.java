package com.moderation;

import com.moderation.engine.ModerationEngine;
import com.moderation.model.Context;
import com.moderation.model.ModerationResult;

import java.util.Scanner;

/**
 * Interactive Content Moderation Engine.
 * Users can enter a User ID, choose a context, and type messages
 * to see them moderated in real time.
 */
public class Main {

    public static void main(String[] args) {
        ModerationEngine engine = new ModerationEngine();
        Scanner scanner = new Scanner(System.in);

        printBanner();

        System.out.print("Enter your User ID: ");
        String userId = scanner.nextLine().trim();
        if (userId.isEmpty()) userId = "guest";

        System.out.println();
        Context context = chooseContext(scanner);

        System.out.println();
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  Type a message and press Enter to moderate.│");
        System.out.println("│  Commands:  'log'  → show audit log          │");
        System.out.println("│             'ctx'  → change context          │");
        System.out.println("│             'user' → change user             │");
        System.out.println("│             'quit' → exit                    │");
        System.out.println("└─────────────────────────────────────────────┘");
        System.out.println();

        while (true) {
            System.out.printf("[%s | %s] > ", userId, context);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            switch (input.toLowerCase()) {
                case "quit":
                case "exit":
                    System.out.println("\nFinal Audit Log:");
                    engine.getAuditLogger().printAll();
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;

                case "log":
                    engine.getAuditLogger().printAll();
                    System.out.printf("  (Total entries: %d | Your entries: %d)%n%n",
                            engine.getAuditLogger().getTotalLogCount(),
                            engine.getAuditLogger().getLogsForUser(userId).size());
                    break;

                case "ctx":
                    System.out.println();
                    context = chooseContext(scanner);
                    System.out.println();
                    break;

                case "user":
                    System.out.print("Enter new User ID: ");
                    String newUser = scanner.nextLine().trim();
                    if (!newUser.isEmpty()) {
                        userId = newUser;
                        System.out.printf("Switched to user: %s%n%n", userId);
                    }
                    break;

                default:
                    // Moderate the input
                    ModerationResult result = engine.moderate(userId, input, context);
                    printResult(result, engine.getStrikeManager().getStrikeCount(userId));
                    break;
            }
        }
    }

    // ---------------------------------------------------------------

    private static Context chooseContext(Scanner scanner) {
        System.out.println("Choose a context:");
        System.out.println("  1. GENERAL      (default, standard rules)");
        System.out.println("  2. EDUCATIONAL  (allows academic discussion of sensitive topics)");
        System.out.println("  3. MEDICAL      (allows clinical terms)");
        System.out.println("  4. GAMING       (stricter: cheating/hacking always blocked)");
        System.out.print("Enter choice (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "2": return Context.EDUCATIONAL;
            case "3": return Context.MEDICAL;
            case "4": return Context.GAMING;
            default:  return Context.GENERAL;
        }
    }

    private static void printResult(ModerationResult result, int strikes) {
        System.out.println();
        String actionDisplay = getActionDisplay(result);
        System.out.println("  ┌─ MODERATION RESULT " + "─".repeat(30));

        if (result.getTriggeredWord() == null) {
            System.out.println("  │  ✓ Status  : CLEAN - No violations found");
        } else {
            System.out.println("  │  ✗ Status  : VIOLATION DETECTED");
            System.out.printf ("  │  ⚑ Triggered: \"%s\" (Severity: %s)%n",
                    result.getTriggeredWord(), result.getSeverity());
        }

        System.out.println("  │  ⚡ Action  : " + actionDisplay);
        System.out.println("  │  ℹ Reason  : " + result.getReason());
        if (strikes > 0) {
            System.out.println("  │  ⚠ Strikes : " + strikes);
        }
        System.out.println("  └" + "─".repeat(51));
        System.out.println();
    }

    private static String getActionDisplay(ModerationResult result) {
        if (result.getAction() == null) return "ALLOW";
        switch (result.getAction()) {
            case ALLOW:               return "✅ ALLOWED";
            case ALLOW_WITH_WARNING:  return "⚠️  ALLOWED WITH WARNING";
            case FLAG:                return "🚩 FLAGGED FOR REVIEW";
            case TEMPORARY_BLOCK:     return "🚫 TEMPORARILY BLOCKED";
            case BLOCK:               return "❌ BLOCKED";
            case PERMANENT_BLOCK:     return "🔴 PERMANENTLY BLOCKED";
            default:                  return result.getAction().toString();
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║      CONTENT MODERATION ENGINE v1.0         ║");
        System.out.println("║  Severity | Text Matching | Strikes | Logs  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }
}
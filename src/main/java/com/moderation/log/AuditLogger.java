// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.moderation.log;

import com.moderation.model.AuditLog;
import com.moderation.model.ModerationAction;
import com.moderation.model.Severity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLogger {
   private final List<AuditLog> logs = new ArrayList();

   public AuditLogger() {
   }

   public void log(String userId, String triggeredWord, ModerationAction action, Severity severity, String reason) {
      AuditLog entry = new AuditLog(userId, triggeredWord, action, severity, reason);
      this.logs.add(entry);
   }

   public List<AuditLog> getAllLogs() {
      return Collections.unmodifiableList(this.logs);
   }

   public List<AuditLog> getLogsForUser(String userId) {
      return (List)this.logs.stream().filter((l) -> l.getUserId().equals(userId)).collect(Collectors.toList());
   }

   public List<AuditLog> getLogsByAction(ModerationAction action) {
      return (List)this.logs.stream().filter((l) -> l.getAction() == action).collect(Collectors.toList());
   }

   public List<AuditLog> getLogsBySeverity(Severity severity) {
      return (List)this.logs.stream().filter((l) -> l.getSeverity() == severity).collect(Collectors.toList());
   }

   public int getTotalLogCount() {
      return this.logs.size();
   }

   public boolean isEmpty() {
      return this.logs.isEmpty();
   }

   public void printAll() {
      if (this.logs.isEmpty()) {
         System.out.println("  No events recorded yet.");
      } else {
         String sep = "=".repeat(55);
         String line = "-".repeat(55);
         System.out.println("  " + sep);
         System.out.println("  AUDIT LOG  (" + this.logs.size() + " entries)");
         System.out.println("  " + line);

         for(AuditLog log : this.logs) {
            System.out.println("  " + log.toString());
         }

         System.out.println("  " + sep);
         System.out.println();
      }
   }

   public void printSummary() {
      String sep = "=".repeat(55);
      String line = "-".repeat(55);
      System.out.println();
      System.out.println("  " + sep);
      System.out.println("  AUDIT SUMMARY");
      System.out.println("  " + line);

      for(ModerationAction action : ModerationAction.values()) {
         long count = this.logs.stream().filter((l) -> l.getAction() == action).count();
         if (count > 0L) {
            System.out.printf("  %-24s : %d%n", action, count);
         }
      }

      System.out.println("  " + line);
      System.out.printf("  %-24s : %d%n", "TOTAL", this.logs.size());
      System.out.println("  " + sep);
      System.out.println();
   }

   public void clearLogs() {
      this.logs.clear();
   }
}

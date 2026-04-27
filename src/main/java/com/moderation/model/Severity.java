package com.moderation.model;

/**
 * Represents the severity level of a banned word.
 *
 * HIGH   -> content is immediately blocked
 * MEDIUM -> content is flagged for human review
 * LOW    -> content is allowed but a warning is attached
 *
 * Module Owner : Person 1 - Core Moderation & Severity Engine
 * Branch       : feature-severity-engine
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH
}

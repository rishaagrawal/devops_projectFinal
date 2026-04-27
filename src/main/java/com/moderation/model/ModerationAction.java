package com.moderation.model;

/**
 * All possible moderation outcomes the engine can return.
 *
 * ALLOW              - content is clean, no action taken
 * ALLOW_WITH_WARNING - minor violation; user warned but message goes through
 * FLAG               - content sent for human review; not yet blocked
 * TEMPORARY_BLOCK    - user blocked temporarily (2nd-strike behaviour)
 * BLOCK              - content blocked outright (HIGH severity default)
 * PERMANENT_BLOCK    - user permanently banned (3rd+ strike behaviour)
 *
 * Module Owner : Person 1 - Core Moderation & Severity Engine
 * Branch       : feature-severity-engine
 */
public enum ModerationAction {
    ALLOW,
    ALLOW_WITH_WARNING,
    FLAG,
    TEMPORARY_BLOCK,
    BLOCK,
    PERMANENT_BLOCK
}

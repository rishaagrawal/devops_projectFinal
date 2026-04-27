package com.moderation.model;

/**
 * The platform context in which the moderation request is made.
 *
 * GENERAL     - default; standard severity rules apply
 * EDUCATIONAL - academic/study setting; some HIGH/MEDIUM words are whitelisted
 * MEDICAL     - clinical setting; medical terms (drug, overdose) are allowed
 * GAMING      - gaming platform; cheating/hacking terms are extra-blocked
 *
 * Module Owner : Person 3 - Strike & Context Rule Manager
 * Branch       : feature-strikes-context
 */
public enum Context {
    GENERAL,
    EDUCATIONAL,
    MEDICAL,
    GAMING
}

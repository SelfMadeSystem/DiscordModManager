package dev.selfmadesystem.dmm.installer.step

enum class StepStatus {
    /**
     * Currently in progress
     */
    ONGOING,

    /**
     * Completed with no errors
     */
    SUCCESSFUL,

    /**
     * Completed with an error
     */
    UNSUCCESSFUL,

    /**
     * Has not yet been ran
     */
    QUEUED
}
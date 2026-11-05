package co.edu.unicauca.review.model;

public enum AssignmentStatus {
    /** Registrada localmente, pendiente de que Lifecycle confirme IN_REVIEW. */
    PENDING,
    /** Lifecycle confirmó la transición a IN_REVIEW. */
    CONFIRMED,
    /** Lifecycle rechazó o no respondió: la asignación NO es válida (compensada). */
    FAILED,
    /** El revisor ya emitió su resultado. */
    COMPLETED
}

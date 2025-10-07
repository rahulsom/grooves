package com.github.rahulsom.grooves.functions;

import com.github.rahulsom.grooves.logging.Trace;

/**
 * A predicate that decides whether to apply more events to the snapshot.
 * Some conditions may require that the snapshot is not updated and returned as is.
 */
@FunctionalInterface
public interface ApplyMoreEventsPredicate<SnapshotT> {
    /**
     * Determines whether more events should be applied to the current snapshot.
     *
     * @param snapshot the current snapshot to evaluate
     * @return true if more events should be applied, false to stop processing
     */
    @Trace
    boolean invoke(SnapshotT snapshot);
}

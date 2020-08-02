/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wps.executor;

import java.util.Map;
import org.geoserver.threadlocals.ThreadLocalsTransfer;
import org.geoserver.wps.resource.WPSResourceManager;
import org.geotools.process.ProcessException;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

/**
 * Handles the execution of a certain class of processes. Implementations using thread pools should
 * take care of transferring the thread locals inside the thread pool leveraging the {@link
 * ThreadLocalsTransfer} class.
 *
 * @author Andrea Aime - GeoSolutions
 */
public interface ProcessManager {

    /** If this process manager can handle the submission */
    boolean canHandle(Name processName);

    /**
     * Synchronous submission, returns the process outputs. GeoServer will use this call only to run
     * nested processes in a process chain, do not use a thread pool for this one as this will
     * result in a deadlock in case there are enough parallel requests with chained processes (which
     * will result in each request taking more than one execution thread, which are limited, one of
     * the common causes for deadlock) <br>
     * It is responsibility of the implementor to call {@link
     * WPSResourceManager#setCurrentExecutionId(String)} from within the thread that is evaluating
     * the inputs and executing the process to ensure proper temporary resource handling (some
     * inputs will parse during execution and require temporary storage, same goes for certain
     * processes in need to use temporary files)
     *
     * @param executionId The unique identifier generated by GeoServer for this execution
     * @param processName The name of the process
     * @param inputs The process inputs
     * @param listener The progress listener, that the process will use to report about its
     *     progress, and check for cancellation
     * @return The results
     */
    Map<String, Object> submitChained(
            String executionId,
            Name processName,
            Map<String, Object> inputs,
            ProgressListener listener)
            throws ProcessException;

    /**
     * Asynchronous submission, not blocking. The process outputs can be retrieved using {@link
     * #getOutput(String, long)}. <br>
     * It is responsibility of the implementor to call {@link
     * WPSResourceManager#setCurrentExecutionId(String)} from within the thread that is evaluating
     * the inputs and executing the process to ensure proper temporary resource handling (some
     * inputs will parse during execution and require temporary storage, same goes for certain
     * processes in need to use temporary files)
     *
     * @param processName The name of the process
     * @param inputs The process inputs
     * @param listener The progress listener, that the process will use to report about its
     *     progress, and check for cancellation
     * @param background Whether this submission is for a background process (that can run at a
     *     lower pace) or a foreground one. This is used to differentiate between a synchronous WPS
     *     request, that has a time sensitive HTTP connection associated to it, and a asynchronous
     *     one, in which the client will poll the server for updates
     */
    void submit(
            String executionId,
            Name processName,
            Map<String, Object> inputs,
            ProgressListener listener,
            boolean background)
            throws ProcessException;

    /**
     * Gets the process output. Will block the caller for at most "timeout" if the process execution
     * is not complete. Once the output is retrieved the process will be marked as terminated and it
     * won't be possible to get its outputs or status anymore.
     */
    Map<String, Object> getOutput(String executionId, long timeout) throws ProcessException;

    /**
     * Attempts to cancel the process execution. If the process is queued for execution it will be
     * removed from the queue, if it's still running a best effort attempt to stop the process will
     * be made.
     */
    void cancel(String executionId);
}

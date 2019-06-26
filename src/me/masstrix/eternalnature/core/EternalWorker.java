package me.masstrix.eternalnature.core;

/**
 * Any class implementing EternalWorker is defined as a worker class
 * and is used by the engine to shutdown and processes, restart and so on.
 */
public interface EternalWorker {

    /**
     * Start the worker. This should start all processes for the worker.
     */
    void start();

    /**
     * End all processes for the worker now. No process from the worker after this
     * is ran should still be running in the background.
     */
    void end();
}

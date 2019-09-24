/*
 * Copyright 2019 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

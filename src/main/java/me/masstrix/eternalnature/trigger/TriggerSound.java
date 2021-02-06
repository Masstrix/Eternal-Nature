/*
 * Copyright 2021 Matthew Denton
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

package me.masstrix.eternalnature.trigger;

/**
 * Stores the base properties for a sound. This stores the sound as a string allowing for
 * custom sounds, and the volume and pitch.
 */
public class TriggerSound {

    private String sound;
    private float volume;
    private float pitch;

    /**
     * Creates a new instance of a trigger sound.
     *
     * @param sound  name of sound that is played. This is the same as what
     *               you would use when using the <i>/playsound</i> command in game.
     * @param volume volume the sound should be played at.
     * @param pitch  pitch the sound should be played at.
     */
    public TriggerSound(String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}

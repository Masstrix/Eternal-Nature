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

package me.masstrix.eternalnature.util;

import java.io.Serializable;

public class EVector extends org.bukkit.util.Vector implements Serializable {

    public EVector() {
        super();
    }

    public EVector(int x, int y, int z) {
        super(x, y, z);
    }

    public EVector(double x, double y, double z) {
        super(x, y, z);
    }

    public EVector(float x, float y, float z) {
        super(x, y, z);
    }
}

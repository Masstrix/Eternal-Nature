/*
 * Copyright 2020 Matthew Denton
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

package me.masstrix.eternalnature.reflection.nms;

import java.lang.reflect.Method;

// TODO create class to handle boss bars to be able to accept chat components from
//      the bungeecord api. Convert the component into a IChatBaseComponent and
//      store the boss bar as a nms object so the server is not aware of it.
public class NmsBossBar {

    private static Class<?> craftBossBarClass;
    private static Class<?> bossBattleServerClass;
    private static Method getHandle;

    static {

    }

//    private BossBattleServer bar;
//
//    public NmsBossBar(BaseComponent... title) {
//        bar = new BossBattleServer(
//                makeTitleComponent(title),
//                BossBattle.BarColor.BLUE,
//                BossBattle.BarStyle.NOTCHED_12);
//    }
//
//    public NmsBossBar setTitle(BaseComponent... title) {
//        bar.a(makeTitleComponent(title));
//        return this;
//    }
//
//    private IChatBaseComponent makeTitleComponent(BaseComponent... comp) {
//        String jsonText = ComponentSerializer.toString(comp);
//        JsonElement json = new JsonParser().parse(jsonText);
//        return CraftChatMessage.fromStringOrNull(jsonText);
//    }
//
//    public void addPlayer(Player player) {
//        bar.addPlayer(((CraftPlayer) player).getHandle());
//    }
//
//    public void removePlayer(Player player) {
//        PacketPlayOutBoss packet = new PacketPlayOutBoss(PacketPlayOutBoss.Action.ADD, bar);
//    }
}

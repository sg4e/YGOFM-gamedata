/*
 * The MIT License (MIT)
 * Copyright (c) 2024 sg4e
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package moe.maika.ygofm.gamedata.examples;

import moe.maika.ygofm.gamedata.*;

import java.util.*;

public class CheckPoolExample {
    public static void main(String[] args) {
        FMDB db = FMDB.getInstance();
        Card raigeki = db.getCard(337);
        Set<Duelist> duelists = db.getAllDuelists();
        Set<Duelist> dropsRaigeki = new HashSet<>();
        for (Duelist d : duelists) {
            for(Pool.Type type : new Pool.Type[] {Pool.Type.SA_POW, Pool.Type.SA_TEC, Pool.Type.BCD}) {
                Pool pool = d.getPool(type);
                if(pool.getEntry(raigeki) != null) {
                    dropsRaigeki.add(d);
                }
            }
        }
        System.out.println(dropsRaigeki);
    }
}

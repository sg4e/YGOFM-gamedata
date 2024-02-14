/*
 * The MIT License
 *
 * Copyright 2024 sg4e.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package moe.maika.ygofm.gamedata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A representation of a card in Forbidden Memories. Do not instantiate this class;
 * use {@link FMDB} to obtain card objects.
 * @author sg4e
 */
public record Card(
    @JsonProperty("cardId")
    int id,
    @JsonProperty("cardName")
    String name,
    @JsonProperty("starchipCost")
    int starchips,
    int level,
    int attack,
    int defense,
    @JsonProperty("guardianStar1")
    GuardianStar firstGuardianStar,
    @JsonProperty("guardianStar2")
    GuardianStar secondGuardianStar,
    String description, 
    String type,
    String attribute,
    String password,
    //values relevant for sorting
    int abcSort,
    int maxSort,
    int atkSort,
    int defSort,
    int typeSort,
    int aiSort,
    //Japanese sort values
    int jpAbcSort,
    int jpMaxSort,
    int jpAtkSort,
    int jpDefSort,
    int jpTypeSort
    )  {
    public boolean canEquip(Card equip, FMDB db) {
        return db.isEquippable(this, equip);
    }

    /**
     * Returns the name of the card
     * @return the name of the card
     */
    @Override
    public String toString() {
        return name;
    }
}

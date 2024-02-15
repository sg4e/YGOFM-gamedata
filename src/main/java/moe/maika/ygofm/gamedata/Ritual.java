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
package moe.maika.ygofm.gamedata;

import java.util.List;
import java.util.ArrayList;

/**
 * A representation of a ritual in Forbidden Memories. All rituals have a ritual card
 * that activates them, exactly three materials, and a result. The material cards do
 * not have to be distinct.
 */
public class Ritual {
    private final int ritualCardId;
    private final int material1, material2, material3;
    private final int resultId;

    private List<Card> materials;
    private Card result;
    private Card ritualCard;

    Ritual(int ritualCardId, int material1, int material2, int material3, int resultId) {
        this.ritualCardId = ritualCardId;
        this.material1 = material1;
        this.material2 = material2;
        this.material3 = material3;
        this.resultId = resultId;
    }

    void initializeReferences(FMDB db) {
        ritualCard = db.getCard(ritualCardId);
        materials = new ArrayList<>(3);
        materials.add(db.getCard(material1));
        materials.add(db.getCard(material2));
        materials.add(db.getCard(material3));
        result = db.getCard(resultId);
    }

    /**
     * Returns the ritual card that activates this ritual.
     * @return the ritual card
     */
    public Card getRitualCard() {
        return ritualCard;
    }

    /**
     * Returns the materials required to perform this ritual.
     * @return a list of length 3 containing the materials for this ritual
     * in no particular order
     */
    public List<Card> getMaterials() {
        return new ArrayList<>(materials);
    }

    /**
     * Returns the result of this ritual.
     * @return the result of this ritual
     */
    public Card getResult() {
        return result;
    }

    int getRitualCardId() {
        return ritualCardId;
    }

    int getMaterial1() {
        return material1;
    }

    int getMaterial2() {
        return material2;
    }

    int getMaterial3() {
        return material3;
    }

    int getResultId() {
        return resultId;
    }
}

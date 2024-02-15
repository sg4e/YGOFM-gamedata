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

class RawRitual {
    private final int ritualCardId;
    private final int material1, material2, material3;
    private final int result;

    RawRitual(int ritualCardId, int material1, int material2, int material3, int result) {
        this.ritualCardId = ritualCardId;
        this.material1 = material1;
        this.material2 = material2;
        this.material3 = material3;
        this.result = result;
    }

    public int getRitualCardId() {
        return ritualCardId;
    }

    public int getMaterial1() {
        return material1;
    }

    public int getMaterial2() {
        return material2;
    }

    public int getMaterial3() {
        return material3;
    }

    public int getResult() {
        return result;
    }
}

/*
 * The MIT License
 *
 * Copyright 2020 sg4e.
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
package sg4e.ygofm.gamedata;

/**
 *
 * @author sg4e
 */
public class RNG {
    /*
    Quoted from GenericMadScientist in the FM discord:
    
    RNG:
    rand is implemented as follows:
    int rand(void)
    {
        seed = 0x41C64E6D * seed + 0x3039;
        return (seed >> 16) & 0x7FFF;
    }

    seed is a 32-bit value whose initial value at game boot is 0x55555555.
    */
    
    private int seed;
    
    public RNG() {
        this(0x55555555);
    }
    
    public RNG(int initialSeed) {
        this.seed = initialSeed;
    }
    
    public RNG(RNG toCopy) {
        this(toCopy.getSeed());
    }
    
    public int rand() {
        seed = 0x41C64E6D * seed + 0x3039;
        return (seed >>> 16) & 0x7FFF;
    }
    
    public int getSeed() {
        return seed;
    }
    
}

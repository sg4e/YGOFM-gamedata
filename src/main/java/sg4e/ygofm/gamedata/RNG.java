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
package sg4e.ygofm.gamedata;

/**
 * An implementation of the RNG used in Forbidden Memories. The constructors of
 * this class are not particularly useful to the end user. Consider using the
 * {@link SeedSearch} class to explore seeds for RNG manipulation.
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
    
    private int seed, delta;
    
    /**
     * Creates a new RNG with the default seed value (0x55555555) and delta of 0.
     */
    public RNG() {
        this(0x55555555, 0);
    }
    
    /**
     * Creates a new RNG with the given seed and delta values. This constructor
     * does not actually advance the RNG state and simply initializes its internal
     * state to the given values. Use {@link #fromDelta(int)} to simulate actual RNG
     * advancement.
     * @param seed the initial seed value
     * @param delta the number of {@code rand()} calls that have been made
     */
    public RNG(int seed, int delta) {
        this.seed = seed;
        this.delta = delta;
    }
    
    /**
     * Creates a new RNG with the same seed and delta as the given RNG.
     * @param toCopy the RNG to copy
     */
    public RNG(RNG toCopy) {
        this(toCopy.getSeed(), toCopy.getDelta());
    }
    
    /**
     * Generates a random number and updates internal state exactly how FM does.
     * @return a random number
     */
    public synchronized int rand() {
        delta++;
        seed = 0x41C64E6D * seed + 0x3039;
        return (seed >>> 16) & 0x7FFF;
    }
    
    /**
     * Gets the current seed value.
     * @return the current seed value
     */
    public synchronized int getSeed() {
        return seed;
    }
    
    /**
     * Gets the number of {@code rand()} calls that have been made.
     * @return the number of {@code rand()} calls
     */
    public synchronized int getDelta() {
        return delta;
    }
    
    /**
     * Creates a new RNG seeded with the default value of 0x55555555 and advances
     * the RNG state by the given number of {@code rand()} calls.
     * @param delta how many {@code rand()} calls to make
     * @return a new RNG with the given seed value
     */
    public static RNG fromDelta(int delta) {
        //can be implemented via a lookup table for workloads that rather use deltas
        RNG rng = new RNG();
        for(int i = 0; i < delta; i++) {
            rng.rand();
        }
        return rng;
    }
    
}

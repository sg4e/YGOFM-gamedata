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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A utility for RNG Manip to search for seeds that produce a given list of draws. Use the builder to
 * configure the search and then call {@link #search()} to perform the search. The search is
 * parallelized and will use as many threads as the system has available. The search space is
 * defined by the start and end values, and the initial seed is used as the starting point for the
 * search. The search space is defined by the number of calls to {@code rand()} to consider. The
 * default search space is 5,000,000, but this can be changed with the {@link Builder#withSpace(int, int)}
 * method. The search space is divided evenly among the available threads, so the more threads
 * available, the faster the search will complete. The search can be cancelled at any time by calling
 * {@link #cancel()}. Note that even with modern processors and parallelization, the search can take
 * a few seconds to complete with the default search space, and longer with larger search spaces.
 * <p>
 * This class is NOT thread-safe. The class will parallelize its own search onto a worker pool of threads
 * in a thread-safe manner, but any external parallelization is not safe. Each search should be performed
 * with a new instance of this class and a new builder.
 * @author sg4e
 */
public class SeedSearch {
    
    private final Deck deck;
    private final List<Card> drawnCards;
    private Comparator<? super Card> sort = Deck.CARD_ID_ORDER;
    /**
     * How many {@code rand()} calls to consider when searching for a seed.
     */
    public static final int DEFAULT_SEARCH_SPACE = 5_000_000;
    private int spaceStart = 0;
    private int spaceEnd = DEFAULT_SEARCH_SPACE;
    private Runnable iterCallback = null;
    private Consumer<RNG> hitCallback = null;
    private RNG initialSeed = new RNG();
    private volatile boolean cancel = false;
    
    private SeedSearch(Deck d, List<Card> drawnCards) {
        if(drawnCards.size() > Deck.DECK_SIZE)
            throw new IllegalArgumentException("Cards drawn exceeds deck size: " + drawnCards.size());
        deck = d;
        this.drawnCards = drawnCards;
    }

    /**
     * Gets the starting value of the seed space to explore, as set by the builder.
     * @return the start of the seed space to explore
     */
    public int getSpaceStart() {
        return spaceStart;
    }

    /**
     * Gets the ending value of the seed space to explore, as set by the builder.
     * @return the end of the seed space to explore
     */
    public int getSpaceEnd() {
        return spaceEnd;
    }
    
    /**
     * Performs the search.
     * @return a set of seeds that produce the drawn cards specified in the builder
     */
    public Set<RNG> search() {
        //make a copy of the deck and order it to the initial state
        Deck startingDeck = new Deck(deck);
        startingDeck.sort(sort);
        //copy seed to prevent side effects
        RNG seedCopy = new RNG(initialSeed);
        Set<RNG> validSeeds = Collections.synchronizedSet(new HashSet<>());
        //go to offset if not starting at initial seed
        for(int i = 0; i < spaceStart; i++) {
            seedCopy.rand();
        }
        IntStream.range(0, spaceEnd - spaceStart).mapToObj(i -> {
            seedCopy.rand();
            return new RNG(seedCopy);
        }).collect(Collectors.toList()).stream().parallel().forEach(seed -> {
            if(!cancel) {
                /*
                Quoted from GenericMadScientist in the FM discord:

                ORDER OF EVENTS:
                Shuffle player deck, generate AI deck, shuffle AI deck.
                */
                Deck testDeck = new Deck(startingDeck);
                testDeck.shuffle(new RNG(seed));
                if(testDeck.startsWith(drawnCards)) {
                    validSeeds.add(new RNG(seed));
                    if(hitCallback != null)
                        hitCallback.accept(new RNG(seed));
                }
                if(iterCallback != null)
                    iterCallback.run();
            }
        });
        return validSeeds;
    }
    
    /**
     * Cancels the search.
     */
    public void cancel() {
        cancel = true;
    }
    
    /**
     * Gets the length of the search space, i.e., the number of seeds that will be explored.
     * @return the length of the search space
     */
    public int getSpaceLength() {
        return spaceEnd - spaceStart;
    }
    
    /**
     * A builder for {@link SeedSearch} instances. Use a new builder for each search.
     * <p>
     * The builder is not thread-safe and should not be reused after building an instance.
     */
    public static class Builder {
        private final SeedSearch search;
        private boolean built = false;
        
        /**
         * Creates a new builder for a seed search.
         * @param deck the deck
         * @param drawnCards the cards drawn from the deck in the order they were drawn
         */
        public Builder(Deck deck, List<Card> drawnCards) {
            search = new SeedSearch(new Deck(deck), new ArrayList<>(drawnCards));
        }
        
        /**
         * Sets the sort order for the deck. The default is to sort by card ID.
         * @param sorter the sort performed on the Build Deck screen before the duel
         * @return this builder
         */
        public Builder withSort(Comparator<? super Card> sorter) {
            check();
            search.sort = sorter;
            return this;
        }
        
        /**
         * Sets the range of seeds to explore. The default is 0 to 5,000,000.
         * @param end the end of the seed space to explore
         * @return this builder
         */
        public Builder withSpace(int end) {
            return withSpace(0, end);
        }
        
        /**
         * Sets the range of seeds to explore. The default is 0 to 5,000,000.
         * @param start the start of the seed space to explore
         * @param end the end of the seed space to explore
         * @return this builder
         */
        public Builder withSpace(int start, int end) {
            check();
            if(start > end) {
                //rather than flip these, throw an exception because it's likely the calling code has a mistake and the author would want to know
                throw new IllegalArgumentException(String.format("Start value %d is greater than end value %d", start, end));
            }
            search.spaceStart = start;
            search.spaceEnd = end;
            return this;
        }
        
        /**
         * Sets a callback to be run after each iteration of the search. This can be used to provide
         * feedback to the user or to cancel the search.
         * @param callback the callback to run after each iteration of the search
         * @return this builder
         */
        public Builder withCallbackAfterEachIteration(Runnable callback) {
            check();
            search.iterCallback = callback;
            return this;
        }
        
        /**
         * Sets a callback to be run after each successfully found seed. This can be used to provide feedback
         * to the user or to cancel the search.
         * @param callback the callback to run after each hit in the search
         * @return this builder
         */
        public Builder withCallbackAfterEachHit(Consumer<RNG> callback) {
            check();
            search.hitCallback = callback;
            return this;
        }
        
        /**
         * Sets the initial seed to use for the search. The default is 0x55555555
         * (the value initialized after boot on the Konami screen).
         * @param initialSeed the initial seed to use for the search
         * @return this builder
         */
        public Builder withInitialSeed(RNG initialSeed) {
            check();
            search.initialSeed = new RNG(initialSeed);
            return this;
        }
        
        /**
         * Builds the seed search.
         * @return the seed search
         */
        public SeedSearch build() {
            built = true;
            return search;
        }
        
        private void check() {
            if(built)
                throw new IllegalStateException("Builder may not be reused after building an instance");
        }
    }
    
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import lombok.Getter;

/**
 *
 * @author sg4e
 */
public class SeedSearch {
    
    private final Deck deck;
    private final List<Card> drawnCards;
    private Comparator<? super Card> sort = Deck.CARD_ID_ORDER;
    /**
     * How many {@code rand()} calls to consider when searching for a seed. Value to be tuned.
     */
    public static final int DEFAULT_SEARCH_SPACE = 5_000_000;
    @Getter
    private int spaceStart = 0;
    @Getter
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
        }).parallel().forEach(seed -> {
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
    
    public void cancel() {
        cancel = true;
    }
    
    public int getSpaceLength() {
        return spaceEnd - spaceStart;
    }
    
    public static class Builder {
        private final SeedSearch search;
        private boolean built = false;
        
        public Builder(Deck deck, List<Card> drawnCards) {
            search = new SeedSearch(new Deck(deck), new ArrayList<>(drawnCards));
        }
        
        public Builder withSort(Comparator<? super Card> sorter) {
            check();
            search.sort = sorter;
            return this;
        }
        
        public Builder withSpace(int end) {
            return withSpace(0, end);
        }
        
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
        
        public Builder withCallbackAfterEachIteration(Runnable callback) {
            check();
            search.iterCallback = callback;
            return this;
        }
        
        public Builder withCallbackAfterEachHit(Consumer<RNG> callback) {
            check();
            search.hitCallback = callback;
            return this;
        }
        
        public Builder withInitialSeed(RNG initialSeed) {
            check();
            search.initialSeed = new RNG(initialSeed);
            return this;
        }
        
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

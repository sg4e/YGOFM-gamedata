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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author sg4e
 */
public class Deck {

    private final Card[] cards;

    public static final int DECK_SIZE = 40;
    public static final Comparator<Card> CARD_ID_ORDER = (c1, c2) -> c1.getId() - c2.getId();

    public Deck() {
        cards = new Card[DECK_SIZE];
    }
    
    public Deck(Collection<Card> composition) {
        this();
        if(composition.size() != DECK_SIZE)
            throw new IllegalArgumentException(String.format("Deck must contain %s cards; given %s instead", DECK_SIZE, composition.size()));
        composition.toArray(cards);
    }
    
    public Deck(Deck toCopy) {
        cards = Arrays.copyOf(toCopy.cards, DECK_SIZE);
    }
    
    public Card get(int index) {
        if(index > DECK_SIZE) 
            throw new IllegalArgumentException(String.format("Index %s exceeds deck size %s", index, DECK_SIZE));
        return cards[index];
    }
    
    /**
     * 
     * @param start inclusive
     * @param end exclusive
     * @return 
     */
    public List<Card> getRange(int start, int end) {
        if(start >= DECK_SIZE)
            throw new IllegalArgumentException("Starting index " + start + " >= deck size");
        if(end > DECK_SIZE)
            throw new IllegalArgumentException("Ending index " + end + " > deck size");
        if(start > end)
            throw new IllegalArgumentException(String.format("Starting index %s > ending index %s", start, end));
        return Arrays.asList(Arrays.copyOfRange(cards, start, end));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Arrays.deepHashCode(this.cards);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final Deck other = (Deck) obj;
        if(!Arrays.deepEquals(this.cards, other.cards)) {
            return false;
        }
        return true;
    }
    
    public void sort(Comparator<? super Card> comparator) {
        Arrays.sort(cards, comparator);
    }

    public void shuffle(RNG seed, Comparator<? super Card> deckOrderBeforeDuel) {
        /*
        Quoted from GenericMadScientist in the FM discord:
        
        If this is the player's deck, first sort it according to the sort 
        selected for the deck before entering the duel.
         */
        Arrays.sort(cards, deckOrderBeforeDuel);
        shuffle(seed);
    }

    /**
     * Shuffles the deck, with its current order as the starting point. Use this
     * shuffle method for AI decks.
     *
     * @param seed
     */
    public void shuffle(RNG seed) {
        //the FM shuffle algorithm:
        for(int i = 0; i < 160; i++) {
            int x = seed.rand() % 40;
            int y = seed.rand() % 40;
            Card holder = cards[x];
            cards[x] = cards[y];
            cards[y] = holder;
        }
    }
    
    private static class Generator implements Iterable<RNG> {
        
        private final int startSeed, endSeed;
        
        public Generator(int startSeed, int endSeed) {
            this.startSeed = startSeed;
            this.endSeed = endSeed;
        }

        @Override
        public Iterator<RNG> iterator() {
            return new Iterator<RNG>() {
                private int seedInt = startSeed;
                //have to test for loop around/overflow
                private boolean started = false;

                @Override
                public boolean hasNext() {
                    return !(seedInt == endSeed && started);
                }

                @Override
                public RNG next() {
                    if(!hasNext())
                        throw new NoSuchElementException();
                    started = true;
                    return new RNG(seedInt++);
                }
            };
        }
        
    }
    
    public Set<RNG> findPossibleSeeds(Comparator<? super Card> initialState, List<Card> drawnCards) {
        //create generator for all seeds
        List<Iterable<RNG>> generators = IntStream.rangeClosed(0x0, 0x10001)
                .mapToObj(i -> new Generator(0xffff * i, 0xffff * (i+1))).collect(Collectors.toList());
        //add the edge value
        generators.add(Collections.singletonList(new RNG(0xffffffff)));
//        List<Iterable<RNG>> generators = Stream.of(
//                new Generator(0x0, 0x3fffffff),
//                new Generator(0x3fffffff, 0x7fffffff),
//                new Generator(0x7fffffff, 0xafffffff),
//                new Generator(0xafffffff, 0x0)).collect(Collectors.toList());
        return generators.parallelStream().map(iter -> findPossibleSeeds(initialState, drawnCards, iter)).flatMap(Set::stream).collect(Collectors.toSet());
    }
    
    public Set<RNG> findPossibleSeeds(Comparator<? super Card> initialState, List<Card> drawnCards, Iterable<RNG> possibleSeeds) {
        if(drawnCards.size() > DECK_SIZE)
            throw new IllegalArgumentException("Cards drawn exceeds deck size: " + drawnCards.size());
        //make a copy of the deck and order it to the initial state
        Deck startingDeck = new Deck(this);
        startingDeck.sort(initialState);
        Set<RNG> validSeeds = new HashSet<>();
        possibleSeeds.forEach(seed -> {
            /*
            Quoted from GenericMadScientist in the FM discord:
            
            ORDER OF EVENTS:
            Shuffle player deck, generate AI deck, shuffle AI deck.
            */
            Deck testDeck = new Deck(startingDeck);
            //make a copy of the initial state of the seed, so we return its pre-shuffle state
            RNG initialSeed = new RNG(seed);
            testDeck.shuffle(seed);
            if(testDeck.startsWith(drawnCards))
                validSeeds.add(initialSeed);
        });
        return validSeeds;
    }
    
    public boolean startsWith(List<Card> cards) {
        if(cards.size() > DECK_SIZE)
            throw new IllegalArgumentException("List of cards cannot exceed deck size: " + cards.size());
        for(int i = 0, n = cards.size(); i < n; i++) {
            if(!cards.get(i).equals(this.cards[i]))
                return false;
        }
        return true;
    }

    /**
     *
     * @param duelist
     * @param seed
     * @return an unshuffled deck
     */
    public static Deck createDuelistDeck(Duelist duelist, RNG seed) {
        Deck deck = new Deck();
        Pool pool = duelist.getPool(Pool.Type.DECK);
        int i = 0;
        while(i < DECK_SIZE) {
            Card dropped = pool.getDrop(seed);
            //decks are limited to 3 copies of a card
            if(Arrays.stream(deck.cards).filter(Objects::nonNull).filter(c -> c.equals(dropped)).count() < 3L) {
                deck.cards[i++] = dropped;
            }
        }
        return deck;
    }
}

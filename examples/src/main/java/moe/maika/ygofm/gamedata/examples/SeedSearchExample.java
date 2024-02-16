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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public class SeedSearchExample {

    // this is a list of card ids from the player's deck. Order isn't important.
    private static final int[] PLAYER_DECK = new int[] {
        421,
        397,
        285,
        208,
        549,
        289,
        505,
        179,
        209,
        485,
        156,
        387,
        436,
        206,
        237,
        635,
        178,
        410,
        488,
        75,
        336,
        75,
        308,
        176,
        268,
        105,
        209,
        549,
        177,
        589,
        333,
        9,
        267,
        402,
        516,
        608,
        469,
        394,
        198,
        547
    };

    // this is a list of card ids from the drawn cards.
    // The order here IS important. The first card is the one at the left of the hand.
    // the first 5 cards are the starting hand.
    // You can experiment removing cards at the end of the array and see how the
    // number of possible seeds increases.
    private static final int[] DRAWN_CARDS = new int[] {
        421,
        397,
        285,
        208,
        549,
        289,
        505,
        179,
        209,
        485,
        156,
        387,
        436,
        206
    };

    public static final Comparator<Card> DECK_SORT = Deck.TYPE_ORDER;

    public static void main(String[] args) {
        // check for input errors; decks must contain exactly 40 cards
        assert PLAYER_DECK.length == Deck.DECK_SIZE;
        FMDB fmdb = FMDB.getInstance();
        Deck player = new Deck(Arrays.stream(PLAYER_DECK).mapToObj(fmdb::getCard).collect(Collectors.toList()));
        SeedSearch search = new SeedSearch.Builder(player, Arrays.stream(DRAWN_CARDS).mapToObj(fmdb::getCard)
                .collect(Collectors.toList()))
                // choose the ordering used to sort the deck on the Build Deck screen
                // only the FINAL sort matters. The others just increases the RNG calls
                .withSort(DECK_SORT)
                // the default number of seeds is almost always sufficient unless the console has been on for a long time.
                // You can increase it like this:
                //.withSpace(25_000_000)
                .build();
        Set<RNG> possibleSeeds = search.search();  // this could take a few seconds depending on your CPU
        int possibleSeedCount = possibleSeeds.size();
        if(possibleSeedCount == 0) {
            // The default seed search space should find the seed, so this is likely an input error.
            // Alternatively, you can increase the search space in the builder above
            System.out.println("No possible seeds found.");
        }
        else if(possibleSeedCount > 1) {
            System.out.println(String.format("%s possible seeds found. Try drawing more cards.", possibleSeedCount));
            // You could also simulate the AI's deck for all possible seeds and consider all possibilities
        }
        else {
            // there is only one possible seed, so we can simulate the AI's deck
            RNG seed = possibleSeeds.stream().findFirst().get();
            System.out.println(String.format("Seed found: %s", seed.getSeed()));
            // generate the AI's deck and permutation
            // This example uses a real deck that was played against Heishin 1 in Free Duel mode
            generateAiDeck(seed, Duelist.Name.HEISHIN_1);
        }
    }

    public static void generateAiDeck(RNG seed, Duelist.Name duelist) {
        // The simulation changes the RNG object's state, so let's make a copy in case we want to inspect the original later
        RNG copy = new RNG(seed);
        // The player's deck is shuffled with the seed and then the AI's is generated, so we need to replicate that
        Deck player = new Deck(Arrays.stream(PLAYER_DECK).mapToObj(FMDB.getInstance()::getCard).collect(Collectors.toList()));
        player.shuffle(copy, DECK_SORT);
        Deck aiDeck = Deck.createDuelistDeck(FMDB.getInstance().getDuelist(duelist), copy);
        // AI decks don't use a sort
        aiDeck.shuffle(copy);
        // print the AI's deck and the order of the cards after the shuffle
        System.out.println(aiDeck);
    }

}
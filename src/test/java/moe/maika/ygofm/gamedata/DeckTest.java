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

import moe.maika.ygofm.gamedata.Duelist;
import moe.maika.ygofm.gamedata.RNG;
import moe.maika.ygofm.gamedata.Deck;
import moe.maika.ygofm.gamedata.Card;
import moe.maika.ygofm.gamedata.FMDB;
import moe.maika.ygofm.gamedata.SeedSearch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static moe.maika.ygofm.gamedata.Deck.ATTACK_ORDER;
import static moe.maika.ygofm.gamedata.Deck.CARD_ID_ORDER;
import static moe.maika.ygofm.gamedata.Deck.DECK_SIZE;
import static moe.maika.ygofm.gamedata.Deck.TYPE_ORDER;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author sg4e
 */
public class DeckTest {
    
    FMDB db;
    /**
     * Seed from the Heishin 1 duel test case.
     */
    final int seed1 = -1060881141;
    /**
     * Two's complement of {@code seed1}.
     */
    final int seed2 = 1086602507;
    
    final int villager1Seed = 1882591394;
    final int villager3Seed = -1543541870;
    
    static final String HEISHIN_1_DUEL_RESOURCE = "/heishin1.txt";
    
    Deck playersDeck, aisDeck;
    
    
    public DeckTest() {
    }
    
    @BeforeEach
    public void init() {
        db = FMDB.getInstance();
    }
    
    private void loadDecks(String resourceLocation) {
        //get a dump memory dump of a shuffled deck from the game:
        try(Stream<String> stream = Files.lines(Paths.get(getClass().getResource(resourceLocation).toURI()))) {
            List<Card> allCards = stream.map(Integer::parseInt).map(db::getCard).collect(Collectors.toList());
            playersDeck = new Deck(allCards.subList(0, DECK_SIZE));
            aisDeck = new Deck(allCards.subList(DECK_SIZE, DECK_SIZE * 2));
        }
        catch(Exception ex) {
            fail(ex);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { seed1, seed2 })
    public void testShuffle(int seed) {
        loadDecks(HEISHIN_1_DUEL_RESOURCE);
        //delta doesn't matter for this test
        verifyShuffle(new RNG(seed, 0), Duelist.Name.HEISHIN_1, CARD_ID_ORDER);
    }
        
    private void verifyShuffle(RNG seed, Duelist.Name duelist, Comparator<Card> order) {
        RNG realSeed = new RNG(seed);
        Deck regeneratedPlayersDeck = new Deck(playersDeck);
        regeneratedPlayersDeck.shuffle(realSeed, order);
        Deck generatedAiDeck = Deck.createDuelistDeck(db.getDuelist(duelist), realSeed);
        generatedAiDeck.shuffle(realSeed);
        assertEquals(playersDeck, regeneratedPlayersDeck);
        assertEquals(aisDeck, generatedAiDeck);
    }
    
    @ParameterizedTest
    @ValueSource(ints = { seed1, villager1Seed, villager3Seed })
    public void testSeeds(int confirmedSeed) {
        RNG rng = new RNG();
        int seed = 0;
        for(int i = 0; i < SeedSearch.DEFAULT_SEARCH_SPACE; i++) {
            rng.rand();
            seed = rng.getSeed();
            if(seed == confirmedSeed)
                break;
        }
        if(seed != confirmedSeed)
            fail();
    }
    
    @ParameterizedTest
    @MethodSource("provideDeckResources")
    public void testFindPossibleSeeds(String resource, Duelist.Name duelist, Comparator<Card> sorter) {
        loadDecks(resource);
        //these data were taken from a deck that was sorted by card id before entering the duel,
        //so reconstruct starting state, then determine RNG seed with first 3/4 of deck
        List<Card> firstThreeQuarters = playersDeck.getRange(0, DECK_SIZE * 3 / 4);
        Set<RNG> seeds = new SeedSearch.Builder(playersDeck, firstThreeQuarters)
                .withSort(sorter)
                .build()
                .search();
        assertEquals(1, seeds.size());
        //now, take the seed and verify it was used to shuffle the player's deck and generate and shuffle ai's deck
        RNG realSeed = seeds.stream().findFirst().get();
        verifyShuffle(realSeed, duelist, sorter);
    }
    
    private static Stream<Arguments> provideDeckResources() {
        return Stream.of(Arguments.of("/villager1.txt", Duelist.Name.VILLAGER_1, CARD_ID_ORDER),
                Arguments.of("/villager3.txt", Duelist.Name.VILLAGER_3, CARD_ID_ORDER),
                Arguments.of(HEISHIN_1_DUEL_RESOURCE, Duelist.Name.HEISHIN_1, CARD_ID_ORDER),
                Arguments.of("/seto1-attack-sort.txt", Duelist.Name.SETO_1, ATTACK_ORDER),
                Arguments.of("/villager1-random-sort-then-id-sort.txt", Duelist.Name.VILLAGER_1, CARD_ID_ORDER),
                Arguments.of("/heishin1-type-sort-2nd-right.txt", Duelist.Name.HEISHIN_1, TYPE_ORDER)
        );
    }
    
    @Test
    public void testGetAllSorts() {
        assertEquals(11, Deck.getAllSorts().size());
    }
    
    @Test
    public void testSortsPrettyPrint() {
        assertEquals(Deck.ALPHABETICAL_ORDER.toString(), "Alphabetical Sort");
    }
    
    @Test
    public void testToList() {
        List<Card> cards = new ArrayList<>();
        for(int i = 1; i <= Deck.DECK_SIZE; i++) {
            cards.add(db.getCard(i));
        }
        Deck deck = new Deck(cards);
        assertEquals(cards, deck.toList());
        //assert internal array is not editable
        cards.remove(0);
        assertNotEquals(cards, deck.toList());
    }
    
    @Test
    public void testJpSorts() {
        List<Card> cards = new ArrayList<>();
        cards.add(db.getCard(1));
        cards.add(db.getCard(2));
        cards.add(db.getCard(3));
        cards.add(db.getCard(4));
        
        cards.sort(Deck.JAPANESE_ALPHABETICAL_ORDER);
        assertEquals(db.getCard(3), cards.get(0));
        assertEquals(db.getCard(1), cards.get(1));
        assertEquals(db.getCard(4), cards.get(2));
        assertEquals(db.getCard(2), cards.get(3));
    }
    
}

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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
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
        db = new FMDB.Builder().build();
    }
    
    @AfterEach
    public void tearDown() {
        db.close();
    }
    
    private void loadDecks(String resourceLocation) {
        //get a dump memory dump of a shuffled deck from the game:
        try(Stream<String> stream = Files.lines(Paths.get(getClass().getResource(resourceLocation).toURI()))) {
            List<Card> allCards = stream.map(Integer::parseInt).map(db::getCard).collect(Collectors.toList());
            playersDeck = new Deck(allCards.subList(0, Deck.DECK_SIZE));
            aisDeck = new Deck(allCards.subList(Deck.DECK_SIZE, Deck.DECK_SIZE * 2));
        }
        catch(Exception ex) {
            fail(ex);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { seed1, seed2 })
    public void testShuffle(int seed) {
        loadDecks(HEISHIN_1_DUEL_RESOURCE);
        verifyShuffle(seed, Duelist.Name.HEISHIN_1);
    }
        
    private void verifyShuffle(int seed, Duelist.Name duelist) {
        RNG realSeed = new RNG(seed);
        Deck regeneratedPlayersDeck = new Deck(playersDeck);
        regeneratedPlayersDeck.shuffle(realSeed, Deck.CARD_ID_ORDER);
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
        for(int i = 0; i < Deck.SEARCH_SPACE; i++) {
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
    public void testFindPossibleSeeds(String resource, Duelist.Name duelist) {
        loadDecks(resource);
        //these data were taken from a deck that was sorted by card id before entering the duel,
        //so reconstruct starting state, then determine RNG seed with first 3/4 of deck
        List<Card> firstThreeQuarters = playersDeck.getRange(0, Deck.DECK_SIZE * 3 / 4);
        Set<RNG> seeds = playersDeck.findPossibleSeeds(Deck.CARD_ID_ORDER, firstThreeQuarters);
        //seeds.stream().mapToInt(RNG::getSeed).forEach(System.out::println);
        assertEquals(1, seeds.size());
        //now, take the seed and verify it was used to shuffle the player's deck and generate and shuffle ai's deck
        RNG realSeed = seeds.stream().findFirst().get();
        verifyShuffle(realSeed.getSeed(), duelist);
    }
    
    private static Stream<Arguments> provideDeckResources() {
        return Stream.of(Arguments.of("/villager1.txt", Duelist.Name.VILLAGER_1),
                Arguments.of("/villager3.txt", Duelist.Name.VILLAGER_3),
                Arguments.of(HEISHIN_1_DUEL_RESOURCE, Duelist.Name.HEISHIN_1)
        );
    }
    
}

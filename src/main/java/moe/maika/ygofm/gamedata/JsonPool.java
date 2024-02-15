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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class JsonPool {
    private final int poolId;
    private final int duelist;
    private final Pool.Type type;
    private final int cardId;
    private final int probability;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    private JsonPool(
        @JsonProperty("poolId")
        int poolId,
        @JsonProperty("duelist")
        int duelist,
        @JsonProperty("poolType")
        Pool.Type type,
        @JsonProperty("cardId")
        int cardId,
        @JsonProperty("cardProbability")
        int probability) 
        {
        this.poolId = poolId;
        this.duelist = duelist;
        this.type = type;
        this.cardId = cardId;
        this.probability = probability;
    }

    public int getPoolId() {
        return poolId;
    }

    public int getDuelist() {
        return duelist;
    }

    public Pool.Type getType() {
        return type;
    }

    public int getCardId() {
        return cardId;
    }

    public int getProbability() {
        return probability;
    }
}

package com.esotericsoftware.yamlbeans.tokenizer;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TokenizerTest {

    private Token STREAM_START = new Token(TokenType.STREAM_START);
    private Token STREAM_END = new Token(TokenType.STREAM_END);
    private Token VALUE = new Token(TokenType.VALUE);
    private Token KEY = new Token(TokenType.KEY);
    private Token BLOCK_MAPPING_START = new Token(TokenType.BLOCK_MAPPING_START);
    private Token BLOCK_END = new Token(TokenType.BLOCK_END);

    /**
     * Purpose: Getting the next token correctly
     * Input: getNextToken() gets the next token and moves to the next token
     * Expected:
     * return Token.STREAM_START
     * return Token.BLOCK_MAPPING_START
     * return Token.KEY
     * ...
     * return Token.STREAM.END
     * return null
     */
    @Test
    public void testGetNextToken() throws Exception {
        Tokenizer tokenizer = new Tokenizer(new FileReader("test/test1.yml"));
        assertEquals(tokenizer.getNextToken() + "", STREAM_START + "");
        assertEquals(tokenizer.getNextToken() + "", BLOCK_MAPPING_START + "");
        assertEquals(tokenizer.getNextToken() + "", KEY + "");
        assertEquals(tokenizer.getNextToken() + "", "<scalar value='12' plain='true' style=''>");
        assertEquals(tokenizer.getNextToken() + "", VALUE + "");
        assertEquals(tokenizer.getNextToken() + "", "<scalar value='13' plain='true' style=''>");
        assertEquals(tokenizer.getNextToken() + "", BLOCK_END + "");
        assertEquals(tokenizer.getNextToken() + "", STREAM_END + "");
        assertNull(tokenizer.getNextToken());
    }

    /**
     * Purpose: Ensuring that the constructor Tokenizer(Reader) is working properly
     * Input: Tokenizer(Reader) FileReader("test/test1.yml") -> Tokenizer(FileReader("test/test1.yml"))
     * Expected:
     * peekNextToken() = Token.STREAM_START
     * Tokenizer(BufferedReader(FileReader("test/test1.yml"))) = Tokenizer(FileReader("test/test1.yml"))
     * Tokenizer(null) throws IllegalArgumentException.class
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTokenizerReader() throws Exception {
        Tokenizer tokenizer = new Tokenizer(new FileReader("test/test1.yml"));
        assertEquals(tokenizer.peekNextToken() + "", STREAM_START + "");
        Tokenizer tokenizer_nbuffered = new Tokenizer((new BufferedReader(new FileReader("test/test1.yml"))));

        Iterator tokenizer_iter = tokenizer.iterator();
        Iterator tokenizer_nbuffered_iter = tokenizer_nbuffered.iterator();

        while (tokenizer_iter.hasNext() || tokenizer_nbuffered_iter.hasNext()) {
            assertEquals(tokenizer_iter.next() + "", tokenizer_nbuffered_iter.next() + "");
        }

        Tokenizer tokenizer_null = new Tokenizer((FileReader) null);
    }

    /**
     * Purpose: Ensuring that the constructor Tokenizer(String) is working properly
     * Input: Tokenizer(String) FileReader("12: 13") = Tokenizer(FileReader("test/test1.yml")), The content of "test/test1.yml" is "12: 13"
     * Expected:
     * Tokenizer(FileReader("12: 13")) = Tokenizer(FileReader("test/test1.yml")), The content of "test/test1.yml" is "12: 13"
     */
    @Test
    public void testTokenizerString() throws Exception {
        Iterator tokenizer_iter = new Tokenizer(new FileReader("test/test1.yml")).iterator();
        Iterator tokenizer_string_iter = new Tokenizer("12: 13").iterator();

        while (tokenizer_iter.hasNext() || tokenizer_string_iter.hasNext()) {
            assertEquals(tokenizer_iter.next() + "", tokenizer_string_iter.next() + "");
        }
    }

    /**
     * Purpose: peeking the next token correctly
     * Input: peekNextToken() peeks the next token and does not move to the next token
     * Expected:
     * return Token.STREAM_START
     * (moves by another function)
     * return Token.BLOCK_MAPPING_START
     * (moves by another function)
     * return Token.KEY
     * (moves by another function)
     * ...
     * return Token.STREAM.END
     * (moves by another function)
     * return null
     */
    @Test
    public void testPeekNextToken() throws Exception {
        Tokenizer tokenizer = new Tokenizer(new FileReader("test/test1.yml"));
        assertEquals(tokenizer.peekNextToken() + "", STREAM_START + "");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", BLOCK_MAPPING_START + "");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", KEY + "");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", "<scalar value='12' plain='true' style=''>");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", VALUE + "");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", "<scalar value='13' plain='true' style=''>");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", BLOCK_END + "");
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextToken() + "", STREAM_END + "");
        tokenizer.getNextToken();
        assertNull(tokenizer.peekNextToken());
    }

    /**
     * Purpose: peeking the type of the next token correctly
     * Input: peekNextToken() peeks the type of the next token and does not move to the next token
     * Expected:
     * return TokenType.STREAM_START
     * (moves by another function)
     * return TokenType.BLOCK_MAPPING_START
     * (moves by another function)
     * return TokenType.KEY
     * (moves by another function)
     * ...
     * return TokenType.STREAM.END
     * (moves by another function)
     * return null
     */
    @Test
    public void testPeekNextTokenType() throws Exception {
        Tokenizer tokenizer = new Tokenizer(new FileReader("test/test1.yml"));
        assertEquals(tokenizer.peekNextTokenType(), TokenType.STREAM_START);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.BLOCK_MAPPING_START);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.KEY);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.SCALAR);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.VALUE);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.SCALAR);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.BLOCK_END);
        tokenizer.getNextToken();
        assertEquals(tokenizer.peekNextTokenType(), TokenType.STREAM_END);
        tokenizer.getNextToken();
        assertNull(tokenizer.peekNextTokenType());
    }


    /**
     * Purpose: Ensuring that the iterator's functions are working properly
     * Input: iterator().hasNext() returns the same value as (Tokenizer.peekNextToken != null)
     * iterator().next() returns the same value as Tokenizer.getNextToken()
     * Expected:
     * Tokenizer.iterator().hasNext() = (Tokenizer.peekNextToken != null)
     * Tokenizer.iterator().next() = Tokenizer.getNextToken()
     * Tokenizer.iterator().remove() throws UnsupportedOperationException.class
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testIterator() throws Exception {
        Tokenizer tokenizer = new Tokenizer(new FileReader("test/test1.yml"));
        Iterator iter = new Tokenizer(new FileReader("test/test1.yml")).iterator();
        assertEquals(iter.hasNext(), tokenizer.peekNextToken() != null);
        assertEquals(iter.next(), tokenizer.getNextToken());
        iter.remove();
    }

    /**
     * Purpose: Getting the next token in a closed reader
     * Input: close() Tokenizer.reader -> Tokenizer.reader.close()
     * Expected:
     * Tokenizer.getNextToken() = Token.STREAM_START
     * Tokenizer.getNextToken() throws TokenizerException.class
     */
    @Test(expected = Tokenizer.TokenizerException.class)
    public void testClose() throws Exception {
        Tokenizer tokenizer = new Tokenizer(new FileReader("test/test1.yml"));
        tokenizer.close();

        assertEquals(tokenizer.getNextToken() + "", STREAM_START + "");
        tokenizer.getNextToken();
    }

}
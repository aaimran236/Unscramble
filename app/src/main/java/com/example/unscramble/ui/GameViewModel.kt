package com.example.unscramble.ui

import androidx.lifecycle.ViewModel
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())

    // Backing property to avoid state updates from other classes
    //The asStateFlow() makes this mutable state flow a read-only state flow.
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord : String

    // Set of words used in the game
    private var usedWords: MutableSet<String> =mutableSetOf()

    var userGuess by mutableStateOf("")
        private set

    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        ///  if (uiState.value.currentWordCount== MAX_NO_OF_WORDS)
        if (usedWords.size== MAX_NO_OF_WORDS){
            //Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        }else{
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }
    }

    ///a helper method to pick a random word from the list and shuffle it
    private fun pickRandomWordAndShuffle(): String{
        currentWord= allWords.random()

        if (usedWords.contains(currentWord)){
            return pickRandomWordAndShuffle()
        }else{
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord=word.toCharArray() //Returns a CharArray containing characters of this string.

        // Scramble the word
        tempWord.shuffle() //Randomly shuffles elements in this array in-place.
        while (String(tempWord).equals(word)){
            tempWord.shuffle()
        }

        ///the shuffled CharArray is converted back into a String and returned.
        return String(tempWord)
    }


    fun resetGame(){
        usedWords.clear()
        _uiState.value= GameUiState(currentScrambledWord =pickRandomWordAndShuffle())
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)

        // Reset user guess
        updateUserGuess("")
    }

    init {
        resetGame()
    }
}
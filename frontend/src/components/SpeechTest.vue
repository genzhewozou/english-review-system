<template>
  <div class="speech-test">
    <h3>Speech Synthesis Test</h3>
    <div class="test-controls">
      <button @click="testSpeech" class="test-btn">
        Test Speech
      </button>
      <button @click="speakTestText" class="test-btn">
        Speak Test Text
      </button>
      <input v-model="testText" placeholder="Enter text to speak" class="test-input">
    </div>
    <div class="test-results">
      <p v-if="testResult">Test result: {{ testResult }}</p>
      <p v-if="error">Error: {{ error }}</p>
      <p v-if="isSpeaking">Speaking...</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useSpeechService } from '../composables/useSpeechService'

export default {
  name: 'SpeechTest',
  setup() {
    const testText = ref('Hello, this is a test of the speech synthesis system.')
    const testResult = ref('')
    const { speakText, isSpeaking, error, testSpeech } = useSpeechService()

    const speakTestText = async () => {
      try {
        await speakText(testText.value)
        testResult.value = 'Speech completed'
      } catch (err) {
        testResult.value = 'Error: ' + err.message
      }
    }

    const runTest = async () => {
      const result = await testSpeech()
      testResult.value = result ? 'Speech synthesis working' : 'Speech synthesis not working'
    }

    return {
      testText,
      testResult,
      speakText,
      isSpeaking,
      error,
      speakTestText,
      testSpeech: runTest
    }
  }
}
</script>

<style scoped>
.speech-test {
  padding: 20px;
  border: 2px solid #409eff;
  border-radius: 8px;
  margin: 20px 0;
  background: #f0f9ff;
}

.test-controls {
  margin: 15px 0;
}

.test-btn {
  padding: 10px 15px;
  margin: 5px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.test-btn:hover {
  background: #66b1ff;
}

.test-input {
  padding: 10px;
  margin: 5px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 300px;
}

.test-results {
  margin-top: 15px;
  padding: 10px;
  background: white;
  border-radius: 4px;
}
</style>
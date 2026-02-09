import { ref } from 'vue'

/**
 * Speech service composable for text-to-speech functionality
 * Comprehensive implementation with multiple approaches and debugging
 */
export function useSpeechService() {
  const isSpeaking = ref(false)
  const error = ref(null)

  /**
   * Test audio volume with a simple tone
   * This ensures the system can produce sound at all
   */
  const testVolume = () => {
    console.log('Testing audio volume...')
    
    try {
      // Create AudioContext if it doesn't exist
      if (!window.audioContext) {
        window.audioContext = new (window.AudioContext || window.webkitAudioContext)()
      }
      
      const ctx = window.audioContext
      const oscillator = ctx.createOscillator()
      const gainNode = ctx.createGain()
      
      oscillator.connect(gainNode)
      gainNode.connect(ctx.destination)
      
      // Create a tone that gradually increases in volume
      oscillator.frequency.value = 440 // A4 note
      gainNode.gain.value = 0.1
      
      oscillator.start()
      
      // Ramp up volume to ensure it's audible
      gainNode.gain.linearRampToValueAtTime(0.3, ctx.currentTime + 0.5)
      
      setTimeout(() => {
        oscillator.stop()
        console.log('Volume test completed - you should have heard a beep')
      }, 1000)
      
      return true
    } catch (e) {
      console.error('Volume test failed:', e)
      return false
    }
  }

  /**
   * Get best available voice for speech synthesis
   * Tries both offline and online voices, prioritizing language matching
   * @param {string} lang - Target language code
   */
  const getBestVoice = (lang = 'en-US') => {
    if (!('speechSynthesis' in window)) return null
    
    const voices = window.speechSynthesis.getVoices()
    console.log('Total available voices:', voices.length)
    
    // Get language prefix (e.g., 'en' from 'en-US')
    const langPrefix = lang.split('-')[0]
    console.log('Looking for voices matching language:', lang, '(prefix:', langPrefix, ')')
    
    // Sort ALL voices by preference:
    // 1. Exact language match (regardless of offline status)
    // 2. Language prefix match (regardless of offline status)
    // 3. Any English voices
    // 4. Any voices
    const sortedVoices = voices.sort((a, b) => {
      const aExactMatch = a.lang === lang
      const bExactMatch = b.lang === lang
      const aPrefixMatch = a.lang.split('-')[0] === langPrefix
      const bPrefixMatch = b.lang.split('-')[0] === langPrefix
      const aIsEnglish = a.lang.startsWith('en-')
      const bIsEnglish = b.lang.startsWith('en-')
      
      // 1. Prioritize exact language matches
      if (aExactMatch && !bExactMatch) return -1
      if (!aExactMatch && bExactMatch) return 1
      
      // 2. Then prioritize language prefix matches
      if (aPrefixMatch && !bPrefixMatch) return -1
      if (!aPrefixMatch && bPrefixMatch) return 1
      
      // 3. Then prioritize English voices for English text
      if (langPrefix === 'en' && aIsEnglish && !bIsEnglish) return -1
      if (langPrefix === 'en' && !aIsEnglish && bIsEnglish) return 1
      
      return 0
    })
    
    if (sortedVoices.length > 0) {
      const bestVoice = sortedVoices[0]
      console.log('Selected best voice:', bestVoice.name, '(', bestVoice.lang, ')', bestVoice.localService ? 'offline' : 'online')
      return bestVoice
    }
    
    return null
  }

  /**
   * Speak text using Web Speech API with voice selection
   * @param {string} text - The text to speak
   * @param {Object} options - Optional configuration
   */
  const speakText = (text, options = {}) => {
    console.log('speakText called with:', text)
    isSpeaking.value = true
    error.value = null

    if ('speechSynthesis' in window) {
      console.log('Speech synthesis is available in this browser')
      
      try {
        // Get best available voice matching the target language
        const targetLang = options.lang || 'en-US'
        const bestVoice = getBestVoice(targetLang)
        
        // Create utterance
        const utterance = new SpeechSynthesisUtterance(text)
        
        // Set properties
        utterance.lang = options.lang || 'en-US'
        utterance.volume = options.volume || 1.0
        utterance.rate = options.rate || 1.0
        utterance.pitch = options.pitch || 1.0
        
        // Set voice if available
        if (bestVoice) {
          utterance.voice = bestVoice
          console.log('Using voice:', bestVoice.name)
        } else {
          console.log('No voice selected - using browser default')
        }
        
        console.log('Created utterance with lang:', utterance.lang)
        
        // Event handlers
        utterance.onstart = () => {
          console.log('✓ Speech started successfully')
        }
        utterance.onend = () => {
          console.log('✓ Speech ended successfully')
          isSpeaking.value = false
        }
        utterance.onerror = (event) => {
          console.error('✗ Speech error:', event.error)
          error.value = event.error
          isSpeaking.value = false
        }
        
        // Direct speak call
        window.speechSynthesis.speak(utterance)
        console.log('Called speechSynthesis.speak()')
        
      } catch (e) {
        console.error('Speech synthesis failed:', e)
        error.value = e.message
        isSpeaking.value = false
      }
    } else {
      console.error('Speech synthesis is not supported in this browser')
      error.value = 'Speech synthesis not supported'
      isSpeaking.value = false
    }
  }

  /**
   * Test speech synthesis with different approaches
   */
  const testSpeech = () => {
    console.log('=== Testing speech synthesis ===')
    
    // Test 1: Simple text
    console.log('Test 1: Simple text')
    speakText('Hello')
    
    // Test 2: Longer text after delay
    setTimeout(() => {
      console.log('Test 2: Longer text')
      speakText('This is a test of the speech synthesis system')
    }, 3000)
    
    return true
  }

  /**
   * Get available voices
   */
  const getAvailableVoices = () => {
    if ('speechSynthesis' in window) {
      const voices = window.speechSynthesis.getVoices()
      console.log('Available voices:', voices.length)
      voices.forEach((voice, index) => {
        console.log(`${index + 1}. ${voice.name} (${voice.lang}) - ${voice.localService ? 'offline' : 'online'}`)
      })
      return voices
    }
    return []
  }

  return {
    speakText,
    testSpeech,
    testVolume,
    getAvailableVoices,
    isSpeaking,
    error
  }
}

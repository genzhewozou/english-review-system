const axios = require('axios');

async function testSubmitAnswer() {
  try {
    const sessionId = 59;
    const cardId = 1;
    const quality = 'DIFFICULT';
    
    console.log('Testing submitAnswer API...');
    console.log('Session ID:', sessionId);
    console.log('Card ID:', cardId);
    console.log('Quality:', quality);
    
    const response = await axios.post(
      `http://localhost:2001/api/reviews/sessions/${sessionId}/answers`,
      {
        cardId: cardId,
        quality: quality,
        responseTimeSeconds: null
      },
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    
    console.log('Response status:', response.status);
    console.log('Response data:', response.data);
    console.log('Test passed!');
    
  } catch (error) {
    console.error('Test failed with error:');
    console.error('Status:', error.response?.status);
    console.error('Data:', error.response?.data);
    console.error('Message:', error.message);
  }
}

testSubmitAnswer();
// Test script to check API service configuration
import { config } from 'dotenv'
import axios from 'axios'

// Load environment variables
config()

console.log('VITE_API_BASE_URL:', process.env.VITE_API_BASE_URL)

// Test the API endpoint
const testApi = async () => {
  try {
    const response = await axios.get(`${process.env.VITE_API_BASE_URL}/todos`)
    console.log('API response status:', response.status)
    console.log('API response data length:', response.data.length)
    console.log('API test successful!')
  } catch (error) {
    console.error('API test failed:', error.message)
    console.error('Error config:', error.config)
  }
}

testApi()

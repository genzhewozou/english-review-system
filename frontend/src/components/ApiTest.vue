<template>
  <div class="api-test">
    <h2>API Connection Test</h2>
    
    <div class="test-buttons">
      <el-button @click="testMaterialAPI" type="primary">Test Material API</el-button>
      <el-button @click="testTextAPI" type="success">Test Text API</el-button>
      <el-button @click="testDirectAPI" type="warning">Test Direct API</el-button>
    </div>

    <div class="results">
      <h3>Results:</h3>
      <pre>{{ results }}</pre>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useApiService } from '../composables/useApiService'
import axios from 'axios'

export default {
  name: 'ApiTest',
  setup() {
    const { apiService } = useApiService()
    const results = ref('')

    const testMaterialAPI = async () => {
      try {
        results.value = 'Testing material API...\n'
        const response = await apiService.get('/materials/1')
        results.value += `SUCCESS: ${JSON.stringify(response.data, null, 2)}\n`
      } catch (error) {
        results.value += `ERROR: ${error.message}\n`
        results.value += `Status: ${error.response?.status}\n`
        results.value += `URL: ${error.config?.url}\n`
      }
    }

    const testTextAPI = async () => {
      try {
        results.value = 'Testing text API...\n'
        const response = await apiService.get('/materials/1/text')
        results.value += `SUCCESS: Content loaded (${response.data.length} characters)\n`
        results.value += `Preview: ${response.data.substring(0, 200)}...\n`
      } catch (error) {
        results.value += `ERROR: ${error.message}\n`
        results.value += `Status: ${error.response?.status}\n`
        results.value += `URL: ${error.config?.url}\n`
      }
    }

    const testDirectAPI = async () => {
      try {
        results.value = 'Testing direct API call...\n'
        const response = await axios.get('http://localhost:2001/api/materials/1/text')
        results.value += `SUCCESS: Direct call worked (${response.data.length} characters)\n`
        results.value += `Preview: ${response.data.substring(0, 200)}...\n`
      } catch (error) {
        results.value += `ERROR: ${error.message}\n`
        results.value += `This is expected due to CORS\n`
      }
    }

    return {
      results,
      testMaterialAPI,
      testTextAPI,
      testDirectAPI
    }
  }
}
</script>

<style scoped>
.api-test {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.test-buttons {
  margin: 2rem 0;
  display: flex;
  gap: 1rem;
}

.results {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 6px;
  margin-top: 2rem;
}

.results pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}
</style>
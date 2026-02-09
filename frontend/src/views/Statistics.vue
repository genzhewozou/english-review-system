<template>
  <div class="statistics">
    <h2 class="page-title">Learning Statistics</h2>
    
    <!-- Date Range Selector -->
    <div class="date-range-selector">
      <div class="card">
        <div class="date-range-header">
          <h3 class="section-title">Select Time Range</h3>
          <div class="date-range-buttons">
            <button
              v-for="range in dateRanges"
              :key="range.value"
              :class="['btn-date-range', { active: selectedDateRange === range.value }]"
              @click="selectedDateRange = range.value"
            >
              {{ range.label }}
            </button>
          </div>
        </div>
        <div class="custom-date-range" v-if="selectedDateRange === 'CUSTOM'">
          <div class="date-inputs">
            <div class="date-input-group">
              <label class="date-label">Start Date</label>
              <input
                type="date"
                v-model="customStartDate"
                class="date-input"
                @change="onCustomDateChange"
              />
            </div>
            <div class="date-input-group">
              <label class="date-label">End Date</label>
              <input
                type="date"
                v-model="customEndDate"
                class="date-input"
                @change="onCustomDateChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Overview Stats -->
    <div class="overview-stats">
      <div class="card">
        <h3 class="section-title">Overview</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ totalCards }}</div>
            <div class="stat-label">Total Cards</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ reviewedCards }}</div>
            <div class="stat-label">Reviewed Cards</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ newCardsAdded }}</div>
            <div class="stat-label">New Cards</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ retentionRate }}%</div>
            <div class="stat-label">Retention Rate</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ totalReviews }}</div>
            <div class="stat-label">Total Reviews</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ averageEase }}</div>
            <div class="stat-label">Avg. Ease</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Progress Charts -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- Daily Reviews Chart -->
        <div class="card chart-card">
          <h3 class="section-title">Daily Reviews</h3>
          <div class="chart-container">
            <canvas ref="dailyReviewsChart"></canvas>
          </div>
        </div>
        
        <!-- Accuracy Chart -->
        <div class="card chart-card">
          <h3 class="section-title">Accuracy Over Time</h3>
          <div class="chart-container">
            <canvas ref="accuracyChart"></canvas>
          </div>
        </div>
      </div>
      
      <div class="chart-row">
        <!-- Review Distribution Chart -->
        <div class="card chart-card">
          <h3 class="section-title">Review Distribution</h3>
          <div class="chart-container">
            <canvas ref="reviewDistributionChart"></canvas>
          </div>
        </div>
        
        <!-- Card Type Distribution Chart -->
        <div class="card chart-card">
          <h3 class="section-title">Card Type Distribution</h3>
          <div class="chart-container">
            <canvas ref="cardTypeDistributionChart"></canvas>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Deck Performance -->
    <div class="deck-performance">
      <div class="card">
        <h3 class="section-title">Deck Performance</h3>
        <div class="deck-performance-table">
          <table>
            <thead>
              <tr>
                <th>Deck</th>
                <th>Total Cards</th>
                <th>Reviewed</th>
                <th>Retention Rate</th>
                <th>Avg. Ease</th>
                <th>Last Reviewed</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="deck in deckPerformance"
                :key="deck.id"
                class="deck-performance-row"
              >
                <td class="deck-name">{{ deck.name }}</td>
                <td>{{ deck.totalCards }}</td>
                <td>{{ deck.reviewedCards }}</td>
                <td>{{ deck.retentionRate }}%</td>
                <td>{{ deck.averageEase }}</td>
                <td>{{ deck.lastReviewed ? formatDate(deck.lastReviewed) : 'Never' }}</td>
              </tr>
              <tr v-if="deckPerformance.length === 0">
                <td colspan="6" class="no-data">No deck data available</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <!-- Problem Areas -->
    <div class="problem-areas">
      <div class="card">
        <h3 class="section-title">Problem Areas</h3>
        <div class="problem-areas-content">
          <div class="problem-cards">
            <h4>Struggling Cards (Lowest Retention)</h4>
            <div class="problem-cards-list">
              <div
                v-for="card in strugglingCards"
                :key="card.id"
                class="problem-card-item"
              >
                <div class="problem-card-front">{{ card.text }}</div>
                <div class="problem-card-stats">
                  <span class="retention-rate">Retention: {{ card.retentionRate }}%</span>
                  <span class="review-count">Reviews: {{ card.reviewCount }}</span>
                </div>
              </div>
              <div v-if="strugglingCards.length === 0" class="no-data">No struggling cards found</div>
            </div>
          </div>
          <div class="review-habits">
            <h4>Review Habits</h4>
            <div class="habit-stats">
              <div class="habit-stat">
                <span class="habit-label">Most Active Time:</span>
                <span class="habit-value">{{ mostActiveTime }}</span>
              </div>
              <div class="habit-stat">
                <span class="habit-label">Average Session Length:</span>
                <span class="habit-value">{{ averageSessionLength }}</span>
              </div>
              <div class="habit-stat">
                <span class="habit-label">Longest Streak:</span>
                <span class="habit-value">{{ longestStreak }} days</span>
              </div>
              <div class="habit-stat">
                <span class="habit-label">Current Streak:</span>
                <span class="habit-value">{{ currentStreak }} days</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useApiService } from '../composables/useApiService'

// Import Chart.js
import Chart from 'chart.js/auto'

export default {
  name: 'Statistics',
  setup() {
    const { apiService } = useApiService()
    
    // Reactive state
    const selectedDateRange = ref('MONTH')
    const customStartDate = ref('')
    const customEndDate = ref('')
    
    // Chart references
    const dailyReviewsChart = ref(null)
    const accuracyChart = ref(null)
    const reviewDistributionChart = ref(null)
    const cardTypeDistributionChart = ref(null)
    
    // Chart instances
    let dailyReviewsChartInstance = null
    let accuracyChartInstance = null
    let reviewDistributionChartInstance = null
    let cardTypeDistributionChartInstance = null
    
    // Date ranges
    const dateRanges = [
      { value: 'WEEK', label: 'Last Week' },
      { value: 'MONTH', label: 'Last Month' },
      { value: 'THREE_MONTHS', label: 'Last 3 Months' },
      { value: 'YEAR', label: 'Last Year' },
      { value: 'CUSTOM', label: 'Custom Range' }
    ]
    
    // Statistics data
    const totalCards = ref(0)
    const reviewedCards = ref(0)
    const newCardsAdded = ref(0)
    const retentionRate = ref(0)
    const totalReviews = ref(0)
    const averageEase = ref(0)
    const deckPerformance = ref([])
    const strugglingCards = ref([])
    const mostActiveTime = ref('')
    const averageSessionLength = ref('')
    const longestStreak = ref(0)
    const currentStreak = ref(0)
    
    // Computed properties
    const startDate = computed(() => {
      const now = new Date()
      let start = new Date()
      
      switch (selectedDateRange.value) {
        case 'WEEK':
          start.setDate(now.getDate() - 7)
          break
        case 'MONTH':
          start.setMonth(now.getMonth() - 1)
          break
        case 'THREE_MONTHS':
          start.setMonth(now.getMonth() - 3)
          break
        case 'YEAR':
          start.setFullYear(now.getFullYear() - 1)
          break
        case 'CUSTOM':
          return customStartDate.value
        default:
          start.setMonth(now.getMonth() - 1)
      }
      
      return start.toISOString().split('T')[0]
    })
    
    const endDate = computed(() => {
      if (selectedDateRange.value === 'CUSTOM') {
        return customEndDate.value
      }
      return new Date().toISOString().split('T')[0]
    })
    
    // Methods
    const loadStatistics = async () => {
      try {
        // In a real implementation, this would fetch data from the backend
        // For now, we'll use mock data
        
        // Mock overview stats
        totalCards.value = 127
        reviewedCards.value = 89
        newCardsAdded.value = 23
        retentionRate.value = 78
        totalReviews.value = 342
        averageEase.value = 2.3
        
        // Mock deck performance
        deckPerformance.value = [
          { id: 1, name: 'English Vocabulary', totalCards: 45, reviewedCards: 38, retentionRate: 82, averageEase: 2.4, lastReviewed: new Date().toISOString() },
          { id: 2, name: 'Business English', totalCards: 32, reviewedCards: 25, retentionRate: 75, averageEase: 2.2, lastReviewed: new Date(Date.now() - 86400000).toISOString() },
          { id: 3, name: 'Daily Phrases', totalCards: 50, reviewedCards: 26, retentionRate: 70, averageEase: 2.1, lastReviewed: new Date(Date.now() - 172800000).toISOString() }
        ]
        
        // Mock struggling cards
        strugglingCards.value = [
          { id: 1, text: 'Entrepreneur', retentionRate: 45, reviewCount: 8 },
          { id: 2, text: 'Meticulous', retentionRate: 52, reviewCount: 6 },
          { id: 3, text: 'Ambiguous', retentionRate: 58, reviewCount: 5 }
        ]
        
        // Mock habits
        mostActiveTime.value = 'Evening (7-9 PM)'
        averageSessionLength.value = '15 minutes'
        longestStreak.value = 12
        currentStreak.value = 5
        
        // Update charts
        updateCharts()
      } catch (error) {
        console.error('Error loading statistics:', error)
      }
    }
    
    const onCustomDateChange = () => {
      loadStatistics()
    }
    
    const updateCharts = () => {
      // Mock data for charts
      const dailyLabels = Array.from({ length: 30 }, (_, i) => {
        const date = new Date()
        date.setDate(date.getDate() - 29 + i)
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
      })
      
      const dailyReviewsData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 20) + 5)
      const accuracyData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 30) + 60)
      
      // Update daily reviews chart
      if (dailyReviewsChart.value) {
        if (dailyReviewsChartInstance) {
          dailyReviewsChartInstance.destroy()
        }
        
        dailyReviewsChartInstance = new Chart(dailyReviewsChart.value, {
          type: 'bar',
          data: {
            labels: dailyLabels,
            datasets: [{
              label: 'Reviews Per Day',
              data: dailyReviewsData,
              backgroundColor: 'rgba(67, 97, 238, 0.7)',
              borderColor: 'rgba(67, 97, 238, 1)',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              y: {
                beginAtZero: true
              }
            }
          }
        })
      }
      
      // Update accuracy chart
      if (accuracyChart.value) {
        if (accuracyChartInstance) {
          accuracyChartInstance.destroy()
        }
        
        accuracyChartInstance = new Chart(accuracyChart.value, {
          type: 'line',
          data: {
            labels: dailyLabels,
            datasets: [{
              label: 'Accuracy (%)',
              data: accuracyData,
              backgroundColor: 'rgba(40, 167, 69, 0.2)',
              borderColor: 'rgba(40, 167, 69, 1)',
              borderWidth: 2,
              tension: 0.3
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              y: {
                beginAtZero: true,
                max: 100
              }
            }
          }
        })
      }
      
      // Update review distribution chart
      if (reviewDistributionChart.value) {
        if (reviewDistributionChartInstance) {
          reviewDistributionChartInstance.destroy()
        }
        
        reviewDistributionChartInstance = new Chart(reviewDistributionChart.value, {
          type: 'pie',
          data: {
            labels: ['Easy', 'Good', 'Hard', 'Again'],
            datasets: [{
              data: [85, 120, 95, 42],
              backgroundColor: [
                'rgba(40, 167, 69, 0.7)',
                'rgba(0, 123, 255, 0.7)',
                'rgba(255, 193, 7, 0.7)',
                'rgba(220, 53, 69, 0.7)'
              ],
              borderColor: [
                'rgba(40, 167, 69, 1)',
                'rgba(0, 123, 255, 1)',
                'rgba(255, 193, 7, 1)',
                'rgba(220, 53, 69, 1)'
              ],
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false
          }
        })
      }
      
      // Update card type distribution chart
      if (cardTypeDistributionChart.value) {
        if (cardTypeDistributionChartInstance) {
          cardTypeDistributionChartInstance.destroy()
        }
        
        cardTypeDistributionChartInstance = new Chart(cardTypeDistributionChart.value, {
          type: 'doughnut',
          data: {
            labels: ['Basic', 'Reverse', 'Basic & Reverse'],
            datasets: [{
              data: [85, 25, 17],
              backgroundColor: [
                'rgba(67, 97, 238, 0.7)',
                'rgba(108, 117, 125, 0.7)',
                'rgba(23, 162, 184, 0.7)'
              ],
              borderColor: [
                'rgba(67, 97, 238, 1)',
                'rgba(108, 117, 125, 1)',
                'rgba(23, 162, 184, 1)'
              ],
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false
          }
        })
      }
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      const date = new Date(dateString)
      return date.toLocaleDateString()
    }
    
    // Lifecycle hooks
    onMounted(() => {
      loadStatistics()
    })
    
    // Watch for date range changes
    watch(selectedDateRange, () => {
      loadStatistics()
    })
    
    return {
      // State
      selectedDateRange,
      customStartDate,
      customEndDate,
      dateRanges,
      dailyReviewsChart,
      accuracyChart,
      reviewDistributionChart,
      cardTypeDistributionChart,
      totalCards,
      reviewedCards,
      newCardsAdded,
      retentionRate,
      totalReviews,
      averageEase,
      deckPerformance,
      strugglingCards,
      mostActiveTime,
      averageSessionLength,
      longestStreak,
      currentStreak,
      
      // Computed
      startDate,
      endDate,
      
      // Methods
      onCustomDateChange,
      formatDate
    }
  }
}
</script>

<style scoped>
.statistics {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  text-align: center;
  color: #2c3e50;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 2rem 0 1.5rem;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.date-range-selector {
  margin-bottom: 2rem;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
  margin-bottom: 2rem;
}

.section-title {
  color: #2c3e50;
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 1.5rem 0;
}

.date-range-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.date-range-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-date-range {
  padding: 0.75rem 1.5rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  color: #2c3e50;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-date-range:hover {
  border-color: #4361ee;
  background: rgba(67, 97, 238, 0.05);
}

.btn-date-range.active {
  background: #4361ee;
  color: white;
  border-color: #4361ee;
}

.custom-date-range {
  margin-top: 1rem;
}

.date-inputs {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.date-input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.date-label {
  font-weight: 600;
  color: #2c3e50;
  font-size: 0.9rem;
}

.date-input {
  padding: 0.75rem 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.date-input:focus {
  outline: none;
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
}

.overview-stats {
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  padding: 1.5rem;
  text-align: center;
  transition: all 0.3s ease;
  border: 2px solid #e9ecef;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: #4361ee;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #4361ee;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: #6c757d;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.charts-section {
  margin-bottom: 2rem;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.chart-card {
  margin-bottom: 0;
}

.chart-container {
  height: 300px;
  position: relative;
}

.deck-performance-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 0.9rem;
}

.deck-performance-row:hover {
  background: rgba(67, 97, 238, 0.05);
}

.deck-name {
  font-weight: 600;
  color: #2c3e50;
}

.no-data {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
  font-style: italic;
}

.problem-areas-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.problem-cards h4,
.review-habits h4 {
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1.5rem 0;
}

.problem-cards-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.problem-card-item {
  padding: 1rem;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.problem-card-item:hover {
  border-color: #dc3545;
  background: rgba(220, 53, 69, 0.05);
}

.problem-card-front {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.problem-card-stats {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #6c757d;
}

.retention-rate {
  font-weight: 600;
  color: #dc3545;
}

.habit-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.habit-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #4361ee;
}

.habit-label {
  font-weight: 600;
  color: #2c3e50;
}

.habit-value {
  color: #4361ee;
  font-weight: 600;
}

/* Responsive design */
@media (max-width: 768px) {
  .date-range-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .date-inputs {
    flex-direction: column;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-row {
    grid-template-columns: 1fr;
  }
  
  .problem-areas-content {
    grid-template-columns: 1fr;
  }
  
  .habit-stat {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}

@media (max-width: 480px) {
  .statistics {
    padding: 10px;
  }
  
  .card {
    padding: 1.5rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .date-range-buttons {
    width: 100%;
  }
  
  .btn-date-range {
    flex: 1;
    text-align: center;
  }
}
</style>

<template>
  <div class="card-test">
    <h1>Text Selection Test</h1>
    <p>This is a test page to debug the text selection functionality for card creation.</p>
    
    <SimpleTextHighlighter />
  </div>
</template>

<script>
import SimpleTextHighlighter from '../components/SimpleTextHighlighter.vue'

export default {
  name: 'CardTest',
  components: {
    SimpleTextHighlighter
  }
}
</script>

<style scoped>
.card-test {
  padding: 2rem;
}
</style>
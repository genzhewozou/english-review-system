<template>
  <div class="highlight-test">
    <h1>Text Highlighting Test</h1>
    <p>This is a test page to debug the text highlighting functionality.</p>
    
    <SimpleTextHighlighter />
  </div>
</template>

<script>
import SimpleTextHighlighter from '../components/SimpleTextHighlighter.vue'

export default {
  name: 'HighlightTest',
  components: {
    SimpleTextHighlighter
  }
}
</script>

<style scoped>
.highlight-test {
  padding: 2rem;
}
</style>
/**
 * Composable for handling animations and transitions
 */
export function useAnimation() {
  /**
   * Fade in animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const fadeIn = (element, options = {}) => {
    const {
      duration = 300,
      delay = 0,
      ease = 'ease-out',
      onComplete = null
    } = options

    element.style.opacity = '0'
    element.style.transition = `opacity ${duration}ms ${ease} ${delay}ms`

    setTimeout(() => {
      element.style.opacity = '1'

      if (onComplete) {
        setTimeout(onComplete, duration)
      }
    }, 10)
  }

  /**
   * Fade out animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const fadeOut = (element, options = {}) => {
    const {
      duration = 300,
      delay = 0,
      ease = 'ease-in',
      onComplete = null
    } = options

    element.style.transition = `opacity ${duration}ms ${ease} ${delay}ms`
    element.style.opacity = '0'

    if (onComplete) {
      setTimeout(onComplete, duration + delay)
    }
  }

  /**
   * Slide in animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const slideIn = (element, options = {}) => {
    const {
      direction = 'up',
      duration = 300,
      delay = 0,
      ease = 'ease-out',
      distance = '20px',
      onComplete = null
    } = options

    // Set initial position
    element.style.opacity = '0'
    
    switch (direction) {
      case 'up':
        element.style.transform = `translateY(${distance})`
        break
      case 'down':
        element.style.transform = `translateY(-${distance})`
        break
      case 'left':
        element.style.transform = `translateX(${distance})`
        break
      case 'right':
        element.style.transform = `translateX(-${distance})`
        break
    }

    element.style.transition = `opacity ${duration}ms ${ease} ${delay}ms, transform ${duration}ms ${ease} ${delay}ms`

    setTimeout(() => {
      element.style.opacity = '1'
      element.style.transform = 'translate(0)'

      if (onComplete) {
        setTimeout(onComplete, duration)
      }
    }, 10)
  }

  /**
   * Slide out animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const slideOut = (element, options = {}) => {
    const {
      direction = 'down',
      duration = 300,
      delay = 0,
      ease = 'ease-in',
      distance = '20px',
      onComplete = null
    } = options

    element.style.transition = `opacity ${duration}ms ${ease} ${delay}ms, transform ${duration}ms ${ease} ${delay}ms`
    element.style.opacity = '0'

    switch (direction) {
      case 'up':
        element.style.transform = `translateY(-${distance})`
        break
      case 'down':
        element.style.transform = `translateY(${distance})`
        break
      case 'left':
        element.style.transform = `translateX(-${distance})`
        break
      case 'right':
        element.style.transform = `translateX(${distance})`
        break
    }

    if (onComplete) {
      setTimeout(onComplete, duration + delay)
    }
  }

  /**
   * Scale in animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const scaleIn = (element, options = {}) => {
    const {
      duration = 300,
      delay = 0,
      ease = 'ease-out',
      startScale = 0.9,
      onComplete = null
    } = options

    element.style.opacity = '0'
    element.style.transform = `scale(${startScale})`
    element.style.transition = `opacity ${duration}ms ${ease} ${delay}ms, transform ${duration}ms ${ease} ${delay}ms`

    setTimeout(() => {
      element.style.opacity = '1'
      element.style.transform = 'scale(1)'

      if (onComplete) {
        setTimeout(onComplete, duration)
      }
    }, 10)
  }

  /**
   * Scale out animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const scaleOut = (element, options = {}) => {
    const {
      duration = 300,
      delay = 0,
      ease = 'ease-in',
      endScale = 0.9,
      onComplete = null
    } = options

    element.style.transition = `opacity ${duration}ms ${ease} ${delay}ms, transform ${duration}ms ${ease} ${delay}ms`
    element.style.opacity = '0'
    element.style.transform = `scale(${endScale})`

    if (onComplete) {
      setTimeout(onComplete, duration + delay)
    }
  }

  /**
   * Bounce animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const bounce = (element, options = {}) => {
    const {
      duration = 500,
      delay = 0,
      scale = 1.1,
      onComplete = null
    } = options

    element.style.transition = `transform ${duration / 2}ms ease-out ${delay}ms, transform ${duration / 2}ms ease-in ${delay + duration / 2}ms`
    element.style.transform = `scale(${scale})`

    setTimeout(() => {
      element.style.transform = 'scale(1)'

      if (onComplete) {
        setTimeout(onComplete, duration + delay)
      }
    }, duration / 2 + delay)
  }

  /**
   * Pulse animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const pulse = (element, options = {}) => {
    const {
      duration = 1000,
      iterations = Infinity,
      scale = 1.05
    } = options

    element.style.animation = `pulse ${duration}ms ease-in-out ${iterations === Infinity ? 'infinite' : iterations}`
    
    // Add pulse keyframes if not already present
    if (!document.getElementById('animation-keyframes')) {
      const style = document.createElement('style')
      style.id = 'animation-keyframes'
      style.textContent = `
        @keyframes pulse {
          0%, 100% {
            transform: scale(1);
          }
          50% {
            transform: scale(${scale});
          }
        }
      `
      document.head.appendChild(style)
    }
  }

  /**
   * Stop pulse animation
   * @param {HTMLElement} element - Target element
   */
  const stopPulse = (element) => {
    element.style.animation = ''
  }

  /**
   * Shake animation
   * @param {HTMLElement} element - Target element
   * @param {Object} options - Animation options
   */
  const shake = (element, options = {}) => {
    const {
      duration = 500,
      onComplete = null
    } = options

    element.style.animation = `shake ${duration}ms ease-in-out`

    // Add shake keyframes if not already present
    if (!document.getElementById('animation-keyframes')) {
      const style = document.createElement('style')
      style.id = 'animation-keyframes'
      style.textContent = `
        @keyframes shake {
          0%, 100% {
            transform: translateX(0);
          }
          10%, 30%, 50%, 70%, 90% {
            transform: translateX(-5px);
          }
          20%, 40%, 60%, 80% {
            transform: translateX(5px);
          }
        }
      `
      document.head.appendChild(style)
    } else if (!document.getElementById('animation-keyframes').textContent.includes('shake')) {
      const style = document.getElementById('animation-keyframes')
      style.textContent += `
        @keyframes shake {
          0%, 100% {
            transform: translateX(0);
          }
          10%, 30%, 50%, 70%, 90% {
            transform: translateX(-5px);
          }
          20%, 40%, 60%, 80% {
            transform: translateX(5px);
          }
        }
      `
    }

    if (onComplete) {
      setTimeout(onComplete, duration)
    }
  }

  /**
   * Create a staggered animation for multiple elements
   * @param {Array<HTMLElement>} elements - Array of elements
   * @param {Function} animationFn - Animation function to use
   * @param {Object} options - Animation options
   */
  const stagger = (elements, animationFn, options = {}) => {
    const {
      delay = 50,
      ...animationOptions
    } = options

    elements.forEach((element, index) => {
      animationFn(element, {
        ...animationOptions,
        delay: animationOptions.delay ? animationOptions.delay + index * delay : index * delay
      })
    })
  }

  /**
   * Create a custom animation
   * @param {HTMLElement} element - Target element
   * @param {Object} properties - CSS properties to animate
   * @param {Object} options - Animation options
   */
  const animate = (element, properties, options = {}) => {
    const {
      duration = 300,
      delay = 0,
      ease = 'ease-out',
      onComplete = null
    } = options

    // Set initial state
    Object.keys(properties).forEach(prop => {
      element.style[prop] = getComputedStyle(element)[prop]
    })

    element.style.transition = Object.keys(properties)
      .map(prop => `${prop} ${duration}ms ${ease} ${delay}ms`)
      .join(', ')

    // Trigger reflow
    element.offsetHeight

    // Set final state
    setTimeout(() => {
      Object.keys(properties).forEach(prop => {
        element.style[prop] = properties[prop]
      })

      if (onComplete) {
        setTimeout(onComplete, duration)
      }
    }, 10)
  }

  /**
   * Get animation classes for Vue transitions
   * @returns {Object} Transition classes
   */
  const getTransitionClasses = () => {
    return {
      fade: {
        enterActiveClass: 'transition-opacity duration-300 ease-out',
        leaveActiveClass: 'transition-opacity duration-300 ease-in',
        enterFromClass: 'opacity-0',
        leaveToClass: 'opacity-0'
      },
      slide: {
        enterActiveClass: 'transition-all duration-300 ease-out',
        leaveActiveClass: 'transition-all duration-300 ease-in',
        enterFromClass: 'opacity-0 translate-y-4',
        leaveToClass: 'opacity-0 translate-y-4'
      },
      scale: {
        enterActiveClass: 'transition-all duration-300 ease-out',
        leaveActiveClass: 'transition-all duration-300 ease-in',
        enterFromClass: 'opacity-0 scale-95',
        leaveToClass: 'opacity-0 scale-95'
      }
    }
  }

  return {
    fadeIn,
    fadeOut,
    slideIn,
    slideOut,
    scaleIn,
    scaleOut,
    bounce,
    pulse,
    stopPulse,
    shake,
    stagger,
    animate,
    getTransitionClasses
  }
}

/**
 * Global animation utilities
 */
export const animationUtils = {
  /**
   * Check if an element is visible in the viewport
   * @param {HTMLElement} element - Target element
   * @returns {boolean} Whether the element is visible
   */
  isInViewport: (element) => {
    const rect = element.getBoundingClientRect()
    return (
      rect.top >= 0 &&
      rect.left >= 0 &&
      rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
      rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    )
  },

  /**
   * Check if an element is partially visible in the viewport
   * @param {HTMLElement} element - Target element
   * @returns {boolean} Whether the element is partially visible
   */
  isPartiallyInViewport: (element) => {
    const rect = element.getBoundingClientRect()
    const windowHeight = window.innerHeight || document.documentElement.clientHeight
    const windowWidth = window.innerWidth || document.documentElement.clientWidth

    return (
      rect.top < windowHeight &&
      rect.bottom > 0 &&
      rect.left < windowWidth &&
      rect.right > 0
    )
  },

  /**
   * Throttle function
   * @param {Function} func - Function to throttle
   * @param {number} limit - Throttle limit in milliseconds
   * @returns {Function} Throttled function
   */
  throttle: (func, limit) => {
    let inThrottle
    return function() {
      const args = arguments
      const context = this
      if (!inThrottle) {
        func.apply(context, args)
        inThrottle = true
        setTimeout(() => inThrottle = false, limit)
      }
    }
  },

  /**
   * Debounce function
   * @param {Function} func - Function to debounce
   * @param {number} wait - Debounce wait time in milliseconds
   * @returns {Function} Debounced function
   */
  debounce: (func, wait) => {
    let timeout
    return function() {
      const args = arguments
      const context = this
      clearTimeout(timeout)
      timeout = setTimeout(() => func.apply(context, args), wait)
    }
  }
}

// WeatherPro AI Chat Module
class WeatherChat {
    constructor() {
        this.isOpen = false;
        this.conversationHistory = [];
        this.isLoading = false;
        this.currentWeatherData = null;
        this.init();
    }

    init() {
        this.createChatUI();
        this.bindEvents();
        this.extractWeatherData();
    }

    createChatUI() {
        // Floating Action Button
        const fab = document.createElement('button');
        fab.className = 'weather-chat-fab';
        fab.id = 'weatherChatFab';
        fab.setAttribute('aria-label', 'Open Weather AI Chat');
        fab.innerHTML = '<i class="bi bi-cloud-lightning-fill"></i>';
        document.body.appendChild(fab);

        // Chat Panel
        const panel = document.createElement('div');
        panel.className = 'weather-chat-panel';
        panel.id = 'weatherChatPanel';
        panel.setAttribute('role', 'dialog');
        panel.setAttribute('aria-label', 'Weather AI Chat');
        panel.innerHTML = `
            <div class="chat-header">
                <div class="chat-title">
                    <i class="bi bi-cloud-lightning-fill"></i>
                    <div class="chat-title-text">
                        <span class="title">Weather AI</span>
                        <span class="subtitle">Your weather assistant</span>
                    </div>
                </div>
                <button class="chat-close-btn" id="chatCloseBtn" aria-label="Close chat">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
            <div class="chat-messages" id="chatMessages" role="log" aria-live="polite"></div>
            <div class="chat-quick-questions" id="chatQuickQuestions" style="display: none;"></div>
            <div class="chat-input-area">
                <div class="chat-input-wrapper">
                    <input 
                        type="text" 
                        class="chat-input" 
                        id="chatInput" 
                        placeholder="Ask about the weather..." 
                        autocomplete="off"
                        aria-label="Ask about the weather"
                    >
                    <button class="chat-send-btn" id="chatSendBtn" aria-label="Send message" disabled>
                        <i class="bi bi-send-fill"></i>
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(panel);
    }

    bindEvents() {
        const fab = document.getElementById('weatherChatFab');
        const panel = document.getElementById('weatherChatPanel');
        const closeBtn = document.getElementById('chatCloseBtn');
        const input = document.getElementById('chatInput');
        const sendBtn = document.getElementById('chatSendBtn');
        const quickQuestions = document.getElementById('chatQuickQuestions');

        fab.addEventListener('click', () => this.toggleChat());
        closeBtn.addEventListener('click', () => this.closeChat());

        input.addEventListener('input', () => {
            sendBtn.disabled = !input.value.trim() || this.isLoading;
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (input.value.trim() && !this.isLoading) {
                    this.sendMessage(input.value.trim());
                }
            }
        });

        sendBtn.addEventListener('click', () => {
            if (input.value.trim() && !this.isLoading) {
                this.sendMessage(input.value.trim());
            }
        });

        // Close on Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.isOpen) {
                this.closeChat();
            }
        });

        // Delegate for quick question buttons
        quickQuestions.addEventListener('click', (e) => {
            const btn = e.target.closest('.quick-question-btn');
            if (btn && !this.isLoading) {
                this.sendMessage(btn.textContent);
            }
        });

        // Close when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (this.isOpen && !panel.contains(e.target) && !fab.contains(e.target)) {
                // Only auto-close on mobile/tablet
                if (window.innerWidth <= 768) {
                    this.closeChat();
                }
            }
        });
    }

    extractWeatherData() {
        // Weather data is already rendered into the page by Thymeleaf.
        // Use client-side DOM selectors only (no Thymeleaf attributes).

        // Helpers for safe parsing
        const parseTemperature = (text) => {
            if (!text) return 0;
            const match = text.match(/(-?\d+(?:\.\d+)?)/);
            return match ? parseFloat(match[1]) : 0;
        };

        const parseInteger = (text) => {
            if (!text) return 0;
            const match = text.match(/(\d+)/);
            return match ? parseInt(match[1], 10) : 0;
        };

        const parseFloatSafe = (text) => {
            if (!text) return 0;
            const match = text.match(/(\d+(?:\.\d+)?)/);
            return match ? parseFloat(match[1]) : 0;
        };

        const cleanText = (text) => text ? text.trim() : '';

        // Find a weather detail card by its title text and return the .card-value element
        const findCardValue = (titleText) => {
            const cards = document.querySelectorAll('.weather-details .glass-card');
            for (const card of cards) {
                const titleEl = card.querySelector('.card-title');
                if (titleEl && titleEl.textContent.trim().toLowerCase() === titleText.toLowerCase()) {
                    return card.querySelector('.card-value');
                }
            }
            return null;
        };

        try {
            // Hero card (current weather summary)
            const cityEl = document.querySelector('.hero-city span');
            const tempEl = document.querySelector('.hero-temp');
            const conditionEl = document.querySelector('.hero-description');
            const feelsLikeHeroEl = document.querySelector('.hero-range strong:last-of-type');

            // Weather detail cards (in .weather-details section)
            const temperatureDetailEl = findCardValue('Temperature');
            const feelsLikeDetailEl = findCardValue('Feels Like');
            const humidityEl = findCardValue('Humidity');
            const windEl = findCardValue('Wind');
            const pressureEl = findCardValue('Pressure');
            const visibilityEl = findCardValue('Visibility');
            const sunriseEl = findCardValue('Sunrise');
            const sunsetEl = findCardValue('Sunset');

            // Get city and country from hero city
            let city = '', country = '';
            if (cityEl && cityEl.textContent) {
                const parts = cityEl.textContent.split(',').map(s => s.trim());
                city = parts[0] || '';
                country = parts[1] || '';
            }

            // Temperature: prefer detail card, fallback to hero
            let temperature = 0;
            if (temperatureDetailEl) {
                temperature = parseTemperature(temperatureDetailEl.textContent);
            } else if (tempEl) {
                temperature = parseTemperature(tempEl.textContent);
            }

            // Feels like: prefer detail card, fallback to hero-range, fallback to temperature
            let feelsLike = temperature;
            if (feelsLikeDetailEl) {
                feelsLike = parseTemperature(feelsLikeDetailEl.textContent);
            } else if (feelsLikeHeroEl) {
                feelsLike = parseTemperature(feelsLikeHeroEl.textContent);
            }

            // Other current weather values from detail cards
            const condition = conditionEl ? cleanText(conditionEl.textContent) : '';
            const humidity = humidityEl ? parseInteger(humidityEl.textContent) : 0;
            const windSpeed = windEl ? parseFloatSafe(windEl.textContent) : 0;
            const pressure = pressureEl ? parseInteger(pressureEl.textContent) : 0;
            const visibility = visibilityEl ? parseFloatSafe(visibilityEl.textContent) : 0;
            const sunrise = sunriseEl ? cleanText(sunriseEl.textContent) : '';
            const sunset = sunsetEl ? cleanText(sunsetEl.textContent) : '';

            // Extract hourly forecast
            const hourlyItems = document.querySelectorAll('.hourly-item');
            const hourlyForecast = [];
            hourlyItems.forEach((item, index) => {
                const timeEl = item.querySelector('.hour-time');
                const tempEl = item.querySelector('.hour-temp span');
                const iconEl = item.querySelector('img');
                const condition = iconEl ? cleanText(iconEl.alt) : '';
                
                if (timeEl && tempEl) {
                    hourlyForecast.push({
                        time: index === 0 ? 'Now' : cleanText(timeEl.textContent),
                        temperature: parseTemperature(tempEl.textContent),
                        condition: condition,
                        precipitationProbability: 0 // Not available in current DOM
                    });
                }
            });

            // Extract daily forecast
            const forecastRows = document.querySelectorAll('.forecast-row');
            const dailyForecast = [];
            forecastRows.forEach(row => {
                const dayEl = row.querySelector('.forecast-day');
                const descEl = row.querySelector('.forecast-description');
                const minEl = row.querySelector('.temp-min');
                const maxEl = row.querySelector('.temp-max');
                const iconEl = row.querySelector('.forecast-icon');
                
                if (dayEl && descEl && minEl && maxEl) {
                    dailyForecast.push({
                        day: cleanText(dayEl.textContent),
                        minTemp: parseTemperature(minEl.textContent),
                        maxTemp: parseTemperature(maxEl.textContent),
                        condition: iconEl ? cleanText(iconEl.alt) : '',
                        description: cleanText(descEl.textContent)
                    });
                }
            });

            this.currentWeatherData = {
                city,
                country,
                temperature,
                feelsLike,
                condition,
                humidity,
                windSpeed,
                pressure,
                visibility,
                sunrise,
                sunset,
                hourlyForecast,
                dailyForecast
            };

            console.log('Weather data extracted:', this.currentWeatherData);
            console.log('Weather AI context validation:', {
                city: this.currentWeatherData?.city,
                temperature: this.currentWeatherData?.temperature,
                feelsLike: this.currentWeatherData?.feelsLike,
                condition: this.currentWeatherData?.condition,
                humidity: this.currentWeatherData?.humidity,
                windSpeed: this.currentWeatherData?.windSpeed,
                pressure: this.currentWeatherData?.pressure,
                visibility: this.currentWeatherData?.visibility,
                hourlyCount: this.currentWeatherData?.hourlyForecast?.length,
                dailyCount: this.currentWeatherData?.dailyForecast?.length
            });
        } catch (e) {
            console.error('Failed to extract weather data:', e);
        }
    }

    toggleChat() {
        if (this.isOpen) {
            this.closeChat();
        } else {
            this.openChat();
        }
    }

    openChat() {
        const panel = document.getElementById('weatherChatPanel');
        const input = document.getElementById('chatInput');
        
        this.isOpen = true;
        panel.classList.add('open');
        
        // Re-extract weather data in case it changed
        this.extractWeatherData();
        
        // Focus input after animation
        setTimeout(() => {
            input.focus();
        }, 300);

        // Show welcome message if first time
        if (this.conversationHistory.length === 0) {
            this.showWelcomeMessage();
        }
    }

    closeChat() {
        const panel = document.getElementById('weatherChatPanel');
        const input = document.getElementById('chatInput');
        
        this.isOpen = false;
        panel.classList.remove('open');
        input.value = '';
        input.blur();
    }

    showWelcomeMessage() {
        const messagesContainer = document.getElementById('chatMessages');
        const quickQuestions = document.getElementById('chatQuickQuestions');
        
        // Welcome message
        this.addMessage('ai', "Hi! I'm Weather AI. Ask me anything about your current weather or forecast.");
        
        // Quick questions
        const questions = [
            "What's the temperature?",
            "Will it rain today?",
            "Should I carry an umbrella?",
            "What is the forecast for tomorrow?"
        ];
        
        quickQuestions.innerHTML = '';
        questions.forEach(q => {
            const btn = document.createElement('button');
            btn.className = 'quick-question-btn';
            btn.textContent = q;
            quickQuestions.appendChild(btn);
        });
        quickQuestions.style.display = 'flex';
    }

    addMessage(role, content) {
        const messagesContainer = document.getElementById('chatMessages');
        const quickQuestions = document.getElementById('chatQuickQuestions');
        
        // Hide quick questions after first user message
        if (role === 'user') {
            quickQuestions.style.display = 'none';
        }
        
        const messageDiv = document.createElement('div');
        messageDiv.className = `chat-message ${role}`;
        
        const avatarIcon = role === 'ai' ? 'bi-cloud-lightning-fill' : 'bi-person-fill';
        const avatarColor = role === 'ai' ? '#64b4ff' : '#FFD54F';
        
        messageDiv.innerHTML = `
            <div class="chat-message-avatar" style="color: ${avatarColor};">
                <i class="bi ${avatarIcon}"></i>
            </div>
            <div class="chat-message-content">${this.escapeHtml(content)}</div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    showTypingIndicator() {
        const messagesContainer = document.getElementById('chatMessages');
        
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chat-message ai chat-typing-message';
        typingDiv.id = 'typingIndicator';
        typingDiv.innerHTML = `
            <div class="chat-message-avatar" style="color: #64b4ff;">
                <i class="bi bi-cloud-lightning-fill"></i>
            </div>
            <div class="chat-typing">
                <span class="typing-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                </span>
                Weather AI is thinking...
            </div>
        `;
        
        messagesContainer.appendChild(typingDiv);
        this.scrollToBottom();
    }

    hideTypingIndicator() {
        const typingIndicator = document.getElementById('typingIndicator');
        if (typingIndicator) {
            typingIndicator.remove();
        }
    }

    async sendMessage(message) {
        const input = document.getElementById('chatInput');
        const sendBtn = document.getElementById('chatSendBtn');
        
        // Add user message
        this.addMessage('user', message);
        this.conversationHistory.push({ role: 'user', content: message });
        
        // Clear input and disable
        input.value = '';
        sendBtn.disabled = true;
        this.isLoading = true;
        
        // Show typing indicator
        this.showTypingIndicator();
        
        try {
            const response = await this.callChatAPI(message);
            this.hideTypingIndicator();
            
            if (response.error) {
                this.addMessage('ai', response.reply);
            } else {
                this.addMessage('ai', response.reply);
                this.conversationHistory.push({ role: 'assistant', content: response.reply });
            }
        } catch (error) {
            this.hideTypingIndicator();
            this.addMessage('ai', "I'm unable to connect to Weather AI right now. Please try again.");
            console.error('Chat error:', error);
        } finally {
            this.isLoading = false;
            sendBtn.disabled = !input.value.trim();
            input.focus();
        }
    }

    async callChatAPI(message) {
        const requestBody = {
            message: message,
            weatherContext: this.currentWeatherData,
            conversationHistory: this.conversationHistory.slice(-10) // Keep last 10 messages
        };

        const response = await fetch('/weather/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatMessages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize when DOM is ready
function initWeatherChat() {
    // Only initialize if weather data exists (not on homepage)
    const weatherExists = document.querySelector('.hero-temp') ||
                          document.querySelector('.weather-details');
    
    if (weatherExists && !window.weatherChat) {
        window.weatherChat = new WeatherChat();
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initWeatherChat);
} else {
    initWeatherChat();
}

// Re-extract weather data when city changes (for AJAX navigation if any)
// Also expose method for manual refresh
window.refreshWeatherChatData = function() {
    if (window.weatherChat) {
        window.weatherChat.extractWeatherData();
    }
};
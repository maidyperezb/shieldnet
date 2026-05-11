class ChatWidget {
    constructor() {
        this.chatWindow = document.getElementById('chat-widget');
        this.toggleBtn = document.getElementById('chat-toggle-btn');
        this.closeBtn = document.getElementById('chat-close-btn');
        this.window = document.getElementById('chat-window');
        this.messagesContainer = document.getElementById('chat-messages');
        this.inputField = document.getElementById('chat-input');
        this.sendBtn = document.getElementById('chat-send-btn');
        this.clearBtn = document.getElementById('chat-clear-btn');
        this.statusArea = document.getElementById('chat-status');
        
        this.isOpen = false;
        this.isLoading = false;
        this.sessionId = this.generateSessionId();
        
        this.init();
    }

    init() {
        // Event listeners
        this.toggleBtn.addEventListener('click', () => this.toggleWindow());
        this.closeBtn.addEventListener('click', () => this.closeWindow());
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.clearBtn.addEventListener('click', () => this.clearHistory());
        
        // Enter para enviar (Shift+Enter para nueva línea)
        this.inputField.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
        
        // Cargar historial al abrir
        this.loadHistory();
    }

    toggleWindow() {
        if (this.isOpen) {
            this.closeWindow();
        } else {
            this.openWindow();
        }
    }

    openWindow() {
        this.window.style.display = 'flex';
        this.isOpen = true;
        this.toggleBtn.style.opacity = '0.5';
        this.inputField.focus();
    }

    closeWindow() {
        this.window.style.display = 'none';
        this.isOpen = false;
        this.toggleBtn.style.opacity = '1';
    }

    async sendMessage() {
        const query = this.inputField.value.trim();
        
        if (!query) {
            alert('Por favor ingresa una pregunta');
            return;
        }
        
        if (this.isLoading) {
            return; // Prevenir doble envío
        }
        
        // Agregar mensaje del usuario
        this.addMessage(query, 'user');
        this.inputField.value = '';
        this.inputField.style.height = 'auto';
        
        // Indicador de carga
        this.setLoading(true);
        this.setStatus('Procesando...');
        
        try {
            // Enviar al backend vía fetch
            const response = await fetch('/api/chat/message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `query=${encodeURIComponent(query)}&sessionId=${this.sessionId}`
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // Procesar streaming SSE
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let fullResponse = '';
            let currentMessageId = this.addMessage('', 'assistant');
            
            while (true) {
                const { done, value } = await reader.read();
                
                if (done) break;
                
                const text = decoder.decode(value);
                const lines = text.split('\n');
                
                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        try {
                            const eventData = JSON.parse(line.substring(6));
                            
                            if (eventData.type === 'message') {
                                fullResponse += eventData.content;
                                this.updateMessage(currentMessageId, fullResponse);
                            } else if (eventData.type === 'error') {
                                this.addMessage(`⚠️ Error: ${eventData.content}`, 'system');
                            }
                        } catch (e) {
                            console.error('Error parsing SSE', e);
                        }
                    }
                }
            }
            
            this.setStatus('Completado');
            
        } catch (error) {
            console.error('Error enviando mensaje:', error);
            this.addMessage(`❌ Error: ${error.message}`, 'system');
            this.setStatus('Error al procesar');
        } finally {
            this.setLoading(false);
        }
    }

    async loadHistory() {
        try {
            const response = await fetch('/api/chat/history');
            const conversaciones = await response.json();
            
            // Limpiar mensajes previos
            this.messagesContainer.innerHTML = '';
            
            // Cargar historial
            conversaciones.slice(-10).forEach(conv => {
                this.addMessage(conv.prompt, 'user');
                this.addMessage(conv.respuesta, 'assistant');
            });
            
        } catch (error) {
            console.error('Error cargando historial:', error);
        }
    }

    async clearHistory() {
        if (!confirm('¿Deseas limpiar todo el historial de conversación?')) {
            return;
        }
        
        try {
            const response = await fetch('/api/chat/history', {
                method: 'DELETE'
            });
            
            if (response.ok) {
                this.messagesContainer.innerHTML = '';
                this.setStatus('Historial limpiado');
            }
        } catch (error) {
            console.error('Error limpiando historial:', error);
            this.setStatus('Error limpiando historial');
        }
    }

    addMessage(content, role) {
        const messageEl = document.createElement('div');
        messageEl.className = `chat-message ${role}`;
        messageEl.textContent = content;
        messageEl.id = `msg-${Date.now()}`;
        
        this.messagesContainer.appendChild(messageEl);
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
        
        return messageEl.id;
    }

    updateMessage(messageId, content) {
        const messageEl = document.getElementById(messageId);
        if (messageEl) {
            messageEl.textContent = content;
            this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
        }
    }

    setLoading(loading) {
        this.isLoading = loading;
        this.sendBtn.disabled = loading;
        this.inputField.disabled = loading;
    }

    setStatus(status) {
        this.statusArea.textContent = status;
    }

    generateSessionId() {
        return 'session-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    }
}

// Inicializar widget cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    new ChatWidget();
});

let mqttClient = null;
let messageCount = 0;

function connect() {
    mqttClient = mqtt.connect('ws://127.0.0.1:15675/ws', {
        protocolVersion: 5,
        clientId: 'web-client-' + Math.random().toString(16).slice(2, 10)
    });

    mqttClient.on('connect', function () {
        console.log('Connected to MQTT broker');
        document.getElementById('connectionStatus').className = 'connection-status connected';
        document.getElementById('connectionStatus').textContent = 'WebSocket: Connected';

        mqttClient.subscribe('sensor/#', function (err) {
            if (err) {
                console.error('Subscribe error:', err);
            } else {
                console.log('Subscribed to sensor topics');
            }
        });
    });

    mqttClient.on('message', function (topic, message) {
        console.log('Message received:', topic, message.toString());

        messageCount++;
        updateCounterDisplay();

        showMessage(topic + ': ' + message.toString());
    });

    mqttClient.on('error', function (error) {
        console.error('MQTT error:', error);
    });

    mqttClient.on('close', function () {
        console.log('MQTT connection closed');
        document.getElementById('connectionStatus').className = 'connection-status disconnected';
        document.getElementById('connectionStatus').textContent = 'WebSocket: Disconnected';
    });
}

function disconnect() {
    if (mqttClient) {
        mqttClient.end();
    }
}

function updateCounterDisplay() {
    document.getElementById('messageCount').textContent = messageCount;
}

function resetCounter() {
    messageCount = 0;
    updateCounterDisplay();
}

function showMessage(message) {
    const messagesDiv = document.getElementById('messages');
    const noMessages = document.getElementById('noMessages');

    if (noMessages) {
        noMessages.remove();
    }

    const messageElement = document.createElement('div');
    messageElement.className = 'message';
    messageElement.textContent = new Date().toLocaleTimeString() + ' - ' + message;
    messagesDiv.insertBefore(messageElement, messagesDiv.firstChild);
}

function publishMessage(event) {
    event.preventDefault();

    const topic = document.getElementById('topic').value;
    const message = document.getElementById('message').value;

    if (topic && message && mqttClient && mqttClient.connected) {
        mqttClient.publish(topic, message, function (err) {
            if (err) {
                console.error('Publish error:', err);
            } else {
                console.log('Message published to', topic);
                document.getElementById('message').value = '1';
            }
        });
    } else {
        console.error('MQTT not connected or missing topic/message');
        alert('MQTT not connected. Please connect first.');
    }
}

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('publishForm').addEventListener('submit', publishMessage);
    document.getElementById('connectBtn').addEventListener('click', function () {
        if (mqttClient && mqttClient.connected) {
            disconnect();
            this.textContent = 'Connect to MQTT';
        } else {
            connect();
            this.textContent = 'Disconnect from MQTT';
        }
    });

    document.getElementById('resetBtn').addEventListener('click', resetCounter);
});